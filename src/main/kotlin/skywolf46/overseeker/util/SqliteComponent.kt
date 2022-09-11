package skywolf46.overseeker.util

import java.lang.reflect.Constructor
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.jvmErasure

class SqliteComponent(val fileName: String) {

    // ----------------------------------------------------
    //             SqliteComponent Implementation
    // ----------------------------------------------------
    val connection: Connection = DriverManager.getConnection("sqlite:jdbc:$fileName")
    private val tables = mutableMapOf<String, SqliteTableAttribute>()

    init {
        // Creating database
        connection.metaData.driverName
    }

    fun construct(table: String, vararg fields: KClass<*>): SqliteTable {
        return SqliteTable(connection, SqliteTableAttribute(table).apply {
            // MAGIC
            // TODO: Add recursive constructor
            TODO()
        })
    }

    fun construct(tableAttribute: SqliteTableAttribute): SqliteTable {
        TODO()
    }

    // ----------------------------------------------------
    //       SqliteComponent Shared Cache / Functions
    // ----------------------------------------------------

    companion object {
        private val tableConstructors = mutableMapOf<Pair<KClass<*>, KClass<*>>, (SqliteComponent) -> Unit>()
        private val sqlFieldDescribers = mutableMapOf<KClass<*>, SqliteFieldDescriber<*>>()

        fun registerConstructor(key: KClass<*>, value: KClass<*>, constructor: (SqliteComponent) -> Unit) {
            tableConstructors[key to value] = constructor
        }

        fun <K : Any> registerSqlFieldDescriber(cls: KClass<K>, describer: SqliteFieldDescriber<K>) {
            sqlFieldDescribers[cls] = describer
        }

        fun <K : Any> getSqlFieldDescriber(
            cls: KClass<K>
        ): SqliteFieldDescriber<K>? {
            return sqlFieldDescribers[cls] as? SqliteFieldDescriber<K>
        }

        init {
            // String class describer
            registerSqlFieldDescriber(String::class, object : SimpleSqliteFieldDescriber<String> {
                override fun write(statement: PreparedStatement, cursor: AtomicInteger, data: String) {
                    statement.setString(cursor.incrementAndGet(), data)
                }

                override fun read(set: ResultSet, cursor: AtomicInteger): String {
                    return set.getString(cursor.incrementAndGet())
                }

                override fun asSqlConstructString(): String {
                    return "VARCHAR"
                }
            })

            // Int class describer
            registerSqlFieldDescriber(Int::class, object : SimpleSqliteFieldDescriber<Int> {
                override fun write(statement: PreparedStatement, cursor: AtomicInteger, data: Int) {
                    statement.setInt(cursor.incrementAndGet(), data)
                }

                override fun read(set: ResultSet, cursor: AtomicInteger): Int {
                    return set.getInt(cursor.incrementAndGet())
                }

                override fun asSqlConstructString(): String {
                    return "INTEGER"
                }

                override fun supportLength(): Boolean {
                    return false
                }
            })
        }
    }

    // ----------------------------------------------------
    //         SqliteComponent Annotation Classes
    // ----------------------------------------------------
    @Target(AnnotationTarget.CLASS)
    annotation class SqlSerializable

    // ----------------------------------------------------
    //         SqliteComponent Extension Classes
    // ----------------------------------------------------

    interface SqliteFieldDescriber<K : Any> {
        fun asSqlConstructString(fieldName: String, length: Int = -1): String

        fun write(statement: PreparedStatement, cursor: AtomicInteger, data: K)

        fun read(set: ResultSet, cursor: AtomicInteger): K
    }

    interface SimpleSqliteFieldDescriber<K : Any> : SqliteFieldDescriber<K> {
        override fun asSqlConstructString(fieldName: String, length: Int): String {
            if (!supportLength() || length == -1) {
                return "$fieldName ${asSqlConstructString()}"
            }
            return "$fieldName ${asSqlConstructString()}($length)"
        }

        fun supportLength(): Boolean {
            return true
        }

        fun asSqlConstructString(): String
    }

    open class SqliteTableAttribute(val tableName: String) {
        private val fields = linkedMapOf<String, SqliteField>()

        fun addField(name: String, cls: KClass<*>, length: Int = -1) {
            fields[name] = SqliteField(name, cls, length)
        }

        fun addField(field: SqliteField) {
            fields[field.fieldName] = field
        }

        fun getFields(): List<Pair<String, SqliteField>> {
            return fields.entries.map { x -> x.key to x.value }
        }

        fun asSqlConstructString(): String {
            return "create table if not exists '$tableName' (" +
                    fields.values.joinToString(separator = ", ") {
                        it.asSqlString()
                    } + ")"
        }
    }

    open class SqliteField(val fieldName: String, val dataClass: KClass<*>, val length: Int = -1) {
        open fun asSqlString(): String {
            return sqlFieldDescribers[dataClass]!!.asSqlConstructString(fieldName, length)
        }
    }

    // ----------------------------------------------------
    //          SqliteComponent Utility Classes
    // ----------------------------------------------------

    open class SqliteTable(protected val connection: Connection, protected val table: SqliteTableAttribute) {
        private val cachedConstructor = mutableMapOf<KClass<*>, Constructor<*>?>()

        fun findAvailableConstructor(klass: KClass<*>): Constructor<*>? {
            if (cachedConstructor.containsKey(klass))
                return cachedConstructor[klass]!!
            val constructor = klass.constructors.find {
                val fields = table.getFields()
                if (fields.size != it.parameters.size)
                    return@find false
                val params = it.parameters
                fields.forEachIndexed { indx, pair ->
                    if (!pair.second.dataClass.isSuperclassOf(params[indx].type.jvmErasure))
                        return@find false
                }
                return@find true
            } ?: return cachedConstructor.getOrPut(klass) { null }
            return cachedConstructor.getOrPut(klass) { constructor.javaConstructor!! }
        }

        fun <T : Any> selectAll(
            klass: KClass<T>,
            order: SqliteOrder = SqliteOrder.NATURAL,
            orderBy: String? = null
        ): List<T> {
            if (checkIsSerializable(klass)) {
                throw IllegalStateException("Cannot serialize to offered class : Class constructor not compatible with current table")
            }
            return connection.prepareStatement("select * from ${table.tableName}").executeQuery().use {
                getSqlFieldDescriber(klass)?.let { describer ->
                    // Deserialize with provided describer..
                    return@use mutableListOf<T>().apply {
                        while (it.next()) {
                            this += describer.read(it, AtomicInteger(0))
                        }
                    }
                }
                return@use mutableListOf<T>().apply {
                    while (it.next()) {
                        this += deserializeFromTable(klass, it)
                    }
                }
            }
        }

        private fun <T : Any> deserializeFromTable(
            klass: KClass<T>,
            resultSet: ResultSet,
            pointer: AtomicInteger = AtomicInteger(0)
        ): T {
            val constructor = findAvailableConstructor(klass)
                ?: throw IllegalStateException("Cannot deserialize data : No compatible constructor")
            val data = Array<Any?>(constructor.parameterCount) { null }
            for (x in data.indices) {
                val paramType = constructor.parameters[x].type.kotlin
                data[x] = sqlFieldDescribers[paramType]?.read(resultSet, pointer)
                    ?: deserializeFromTable(paramType, resultSet, pointer)
            }
            return constructor.newInstance(*data) as T
        }

        private fun checkIsSerializable(klass: KClass<*>): Boolean {
            if (getSqlFieldDescriber(klass) != null) {
                return true
            }
            if (!klass.hasAnnotation<SqlSerializable>()) {
                return false
            }
            return findAvailableConstructor(klass) != null
        }

    }

    enum class SqliteOrder {
        NATURAL, REVERSED
    }

    class SimpleKeyValueTable<V : Any>(tableName: String, valueClass: KClass<V>) : SqliteTableAttribute(tableName) {
        init {
            addField(SqlFieldPrimaryIndex("index"))
            addField("key", String::class, 60)
            addField("value", valueClass)
        }
    }

    class SqlFieldPrimaryIndex(fieldName: String) :
        SqliteField(fieldName, Integer::class, -1) {
        override fun asSqlString(): String {
            return "$fieldName INTEGER PRIMARY KEY AUTOINCREMENT"
        }
    }


}
