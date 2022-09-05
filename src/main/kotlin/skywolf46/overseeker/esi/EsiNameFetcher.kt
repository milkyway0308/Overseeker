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

object EsiNameFetcher {
    private const val ESI_GET_NAME = "https://esi.evetech.net/latest/universe/names/?datasource=tranquility"
    private val parser = JSONParser()
    private val client = HttpClient(CIO)
    private val cachedNames = mutableMapOf<Long, String?>()

    fun getName(id: Long): String? {
        if (id == -1L)
            return null
        return cachedNames[id]
    }

    suspend fun fetchNames(vararg id: Long) {
        val toResolve = id.filter { it != -1L && cachedNames[it] == null }.distinct()
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
            array.fetch().forEach {
                cachedNames[it.of("id")] = it.maybe("name")
            }
        }
    }
}