package skywolf46.overseeker.zkillboard

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import skywolf46.overseeker.zkillboard.raw.KillData
import skywolf46.overseeker.zkillboard.resolved.ResolvedKillData
import java.util.*
import java.util.concurrent.Executors

@Suppress("unused")
object KillBoardFetcher {
    private val parser = JSONParser()
    private val runner = Executors.newFixedThreadPool(1)
    private val resolver = Executors.newFixedThreadPool(10)
    private val listeners = Collections.synchronizedList(mutableListOf<(ResolvedKillData) -> Unit>())

    fun initFetcher() {
        println("-- Killboard fetcher initialization start")
        val client = startClient()
        configureWebSocket(client)
        println("-- Killboard fetcher initialization complete")
    }

    private fun startClient(): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }

    private fun configureWebSocket(client: HttpClient) {
        runner.execute {
            runCatching {
                runBlocking {
                    client.webSocket(method = HttpMethod.Get, host = "zkillboard.com", port = 8080, path = "/websocket") {
                        send(JSONObject().apply {
                            put("action", "sub")
                            put("channel", "killstream")
                        }.toJSONString())
                        while (true) {
                            val msg = incoming.receive() as? Frame.Text ?: continue
                            val data = KillData(parser.parse(msg.readText()) as JSONObject)
                            resolver.execute {
                                val resolved = data.resolveAll()
                                listeners.forEach {
                                    it(resolved)
                                }
                            }
                        }
                    }
                }
            }
            println("Channel connection ended - Retry after 100ms")
            Thread.sleep(100L)
            configureWebSocket(client)
        }
    }

    fun registerListener(unit: (ResolvedKillData) -> Unit) {
        listeners += unit
    }


}
