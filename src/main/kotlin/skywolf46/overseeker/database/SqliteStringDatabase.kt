package skywolf46.overseeker.database

import skywolf46.overseeker.abstraction.CacheQuery
import java.sql.Connection
import java.sql.DriverManager
import kotlin.reflect.KClass

class SqliteStringDatabase(dbName: String) : CacheQuery<String> {
    val connection: Connection

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$dbName.db")
    }


    override fun constructTable(table: String, valueType: KClass<String>): Boolean {
        runCatching {
            connection.createStatement().execute("create table if not exists `$table`(test varchar )  ")
            return true
        }.onFailure {
            it.printStackTrace()
            return false
        }
    }

    override fun getNonExists(table: String, vararg key: String): List<String> {
        val query = "select * from $table where " + key.joinToString(" or value = ") + ";"
        connection.createStatement().executeQuery(query).use {
            val keys = key.toMutableList()
            while (it.next()) {
                keys -= it.getString(1)
            }
            return keys
        }
    }

    override fun cache(table: String, vararg data: Pair<String, String>) {
        connection.prepareStatement("insert into $table values (?, ?)").use {
            for ((key, value) in data) {
                it.setString(1, key)
                it.setString(2, value)
                it.addBatch()
            }
            it.executeBatch()
        }
    }
}