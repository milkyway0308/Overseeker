package skywolf46.overseeker.util

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class SqliteComponent<K, V>(val fileName: String) {

    // ----------------------------------------------------
    //             SqliteComponent Implementation
    // ----------------------------------------------------
    val connection: Connection = DriverManager.getConnection("sqlite:jdbc:$fileName")
    private val tables = mutableMapOf<String, SqliteTableAttribute>()

    init {
        // Creating database
        connection.metaData.driverName
    }

    fun construct(table: String, vararg fields: KClass<*>) {

    }

    // ----------------------------------------------------
    //       SqliteComponent Shared Cache / Functions
    // ----------------------------------------------------

    companion object {
        private val tableConstructors = mutableMapOf<Pair<KClass<*>, KClass<*>>, (SqliteComponent<*, *>) -> Unit>()
        private val sqlFieldDescribers = mutableMapOf<KClass<*>, SqliteFieldDescriber<*>>()

        fun registerConstructor(key: KClass<*>, value: KClass<*>, constructor: (SqliteComponent<*, *>) -> Unit) {
            tableConstructors[key to value] = constructor
        }

        fun <K : Any> registerSqlFieldDescriber(cls: KClass<K>, describer: SqliteFieldDescriber<K>) {
            sqlFieldDescribers[cls] = describer
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

    abstract class SqliteTableAttribute(val tableName: String) {
        private val fields = linkedMapOf<String, SqliteField>()

        fun addField(name: String, cls: KClass<*>, length: Int = -1) {
            fields[name] = SqliteField(name, cls, length)
        }

        fun addField(field: SqliteField) {
            fields[field.fieldName] = field
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
