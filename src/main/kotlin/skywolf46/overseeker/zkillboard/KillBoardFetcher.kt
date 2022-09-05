package skywolf46.overseeker.zkillboard

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
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
    private val resolver = Executors.newFixedThreadPool(1)
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
            runBlocking {
                client.webSocket(method = HttpMethod.Get, host = "zkillboard.com", port = 8080, path = "/websocket") {
                    send(JSONObject().apply {
                        put("action", "sub")
                        put("channel", "killstream")
                    }.toJSONString())
                    while (true) {
                        val msg = incoming.receive() as? Frame.Text ?: continue
                        val data = KillData(parser.parse(msg.readText()) as JSONObject)
                        println("Incoming killmail: ${data.victim.characterId} (Proceed in ${data.processingTime}ms)")
                        resolver.execute {
                            val time = System.currentTimeMillis()
                            println("Resolving started..")
                            val resolved = data.resolveAll()
                            println("Resolving completed on ${System.currentTimeMillis() - time}ms")
                            listeners.forEach {
                                it(resolved)
                            }
                        }
                    }
                }
            }
        }
    }

    fun registerListener(unit: (ResolvedKillData) -> Unit) {
        listeners += unit
    }


}
