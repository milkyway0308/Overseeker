package skywolf46.overseeker

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import skywolf46.overseeker.listener.CommandListener
import skywolf46.overseeker.zkillboard.KillBoardFetcher
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess

object OverSeeker {
    @JvmStatic
    fun main(args: Array<String>) {
        println("OverSeeker - EVE Fleet Killmail Collector")
        init()
    }


    private fun init() {
        println("-- Configuration load start")
        val file = File("config.properties")
        val prop = Properties()
        checkConfiguration(prop, file)
        loadConfiguration(prop, file)
        println("-- Configuration load complete")
        startBot(prop)
        KillBoardFetcher.initFetcher()
    }

    private fun checkConfiguration(properties: Properties, file: File) {
        properties["token"] = "Add discord bot token here"
        properties["playing"] = "Monitoring New Eden"

        if (!file.exists()) {
            println("..Configuration not found. Creating new one.")
            runCatching {
                file.createNewFile()
                FileOutputStream(file).use {
                    properties.store(it, "Overseeker configuration")
                }
            }.onFailure {
                System.err.println("....Failed to create new configuration")
                it.printStackTrace()
                exitProcess(-1)
            }
            println("....New configuration saved on \"${file.path}\"")
        }
    }

    private fun loadConfiguration(properties: Properties, file: File) {
        println("..Loading configuration")
        runCatching {
            FileInputStream(file).use {
                properties.load(it)
            }
        }.onFailure {
            System.err.println("....Failed to load configuration")
            it.printStackTrace()
            exitProcess(-1)
        }
    }


    private fun startBot(prop: Properties) {
        println("-- JDA initialization start")
        val jda = initJda(prop)
        initCommands(prop, jda)
        println("-- JDA initialization complete")
    }

    private fun initJda(prop: Properties): JDA {
        return try {
            JDABuilder.create(prop.getProperty("token"), GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .addEventListeners(CommandListener())
                .setActivity(Activity.playing("Monitoring New Eden"))
                .build()
        } catch (e: Throwable) {
            System.err.println("....Failed to initialize JDA")
            e.printStackTrace()
            exitProcess(-1)
        }
    }

    private fun initCommands(prop: Properties, jda: JDA) {
        jda.upsertCommand("start", "Test Command").queue()
        jda.upsertCommand("clr", "Test Command").queue()
        jda.upsertCommand("overseek", "해당 플레이어에 대한 플릿 킬메일을 수집합니다.").queue()
        jda.upsertCommand("os-config", "오버시커 어플리케이션 서버 설정을 수정합니다.").queue()
        jda.upsertCommand("analyze", "수집된 킬메일을 분석합니다.").queue()
    }
}