package skywolf46.overseeker.esi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.util.concurrent.locks.ReentrantReadWriteLock

object EsiJumpFetcher {

    private const val ESI_GET_ROUTE =
        "https://esi.evetech.net/latest/route/%from%/%to%/?datasource=tranquility&flag=shortest"
    private val client = HttpClient(CIO)
    private val jumps = mutableMapOf<Pair<String, String>, Int>()
    private val lock = ReentrantReadWriteLock()
    private val parser = JSONParser()

    fun loadAll() {
        // Load jump counts
    }

    suspend fun fetchJumps(from: String, to: String): Int {
        if (from to to in jumps) {
            return jumps[from to to]!!
        }
        EsiNameFetcher.fetchIds(from, to)
        client.get(
            ESI_GET_ROUTE.replace("%from%", EsiNameFetcher.getId(from).toString())
                .replace("%to%", EsiNameFetcher.getId(to).toString())
        ).bodyAsText().apply {
            val parsed = parser.parse(this)
            if (parsed !is JSONArray)
                return -1
            jumps[from to to] = parsed.size
        }
        return jumps[from to to]!!
    }
}