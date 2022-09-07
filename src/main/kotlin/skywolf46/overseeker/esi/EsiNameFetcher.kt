package skywolf46.overseeker.esi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import skywolf46.overseeker.util.fetch
import skywolf46.overseeker.util.maybe
import skywolf46.overseeker.util.of
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object EsiNameFetcher {
    private const val ESI_GET_NAME = "https://esi.evetech.net/latest/universe/names/?datasource=tranquility"
    private const val ESI_GET_ID = "https://esi.evetech.net/latest/universe/ids/?datasource=tranquility"
    private val lock = ReentrantReadWriteLock()
    private val parser = JSONParser()
    private val client = HttpClient(CIO)
    private val cachedNames = mutableMapOf<Long, String?>()
    private val cachedIds = mutableMapOf<String, Long>()

    fun getName(id: Long): String? {
        if (id == -1L)
            return null
        return lock.read {
            cachedNames[id]
        }
    }

    suspend fun fetchNames(vararg id: Long) {
        val toResolve = lock.read {
            id.filter { it != -1L && cachedNames[it] == null }
        }.distinct()
        if (toResolve.isEmpty())
            return
        client.post(ESI_GET_NAME) {
            setBody(JSONArray().apply {
                addAll(toResolve)
            }.toJSONString())
        }.bodyAsText().apply {
            val array = parser.parse(this)
            if (array !is JSONArray)
                return
            array.fetch().apply {
                lock.write {
                    forEach {
                        it.maybe<String>("name")?.apply {
                            cachedIds[this] = it.of("id")
                        }
                        cachedNames[it.of("id")] = it.maybe("name")
                    }
                }
            }
        }
    }
    suspend fun fetchIds(vararg id: String) {
        val toResolve = lock.read {
            id.filter { !cachedIds.containsKey(it)}
        }.distinct()
        if (toResolve.isEmpty())
            return
        client.post(ESI_GET_ID) {
            setBody(JSONArray().apply {
                addAll(toResolve)
            }.toJSONString())
        }.bodyAsText().apply {
            val array = parser.parse(this)
            if (array !is JSONObject)
                return
            lock.write {
                // Outer for-each
                array.forEach { _, arr ->
                    if(arr !is JSONArray)
                        return@forEach
                    for(x in arr.fetch()) {
                        x.maybe<Long>("id")?.apply {
                            cachedIds[x.of("name")] = this
                            cachedNames[this] = x.of("name")
                        }
                    }
                }
            }
        }
    }

    fun getId(name: String): Long {
        return cachedIds[name] ?: -1
    }
}