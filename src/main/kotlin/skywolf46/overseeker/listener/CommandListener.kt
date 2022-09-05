package skywolf46.overseeker.listener

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import skywolf46.overseeker.util.compressIsk
import skywolf46.overseeker.zkillboard.KillBoardFetcher
import java.awt.Color
import java.time.format.DateTimeFormatter

class CommandListener : ListenerAdapter() {

    init {
        KillBoardFetcher.registerListener {
            println("Channel null")
            if (tempChannel == null) return@registerListener
            println("Sended")
            tempChannel!!.sendMessage(MessageCreateData.fromEmbeds(EmbedBuilder().apply {
                setColor(Color.RED)
                when {
                    it.description.isAwoxed -> {
                        setTitle("Awoxed!", it.link.zkillboard)
                        setColor(Color.decode("#c42bc4"))
                    }
                    it.description.isNpcKill -> {
                        if (it.attacker.filter { it.isFinalBlow }[0].ship?.contains("Police") == true) {
                            setTitle("Justice Enforcement!", it.link.zkillboard)
                            setColor(Color.decode("#cec7ff"))
                        } else {
                            setTitle("Ratted!", it.link.zkillboard)
                            setColor(Color.decode("#2b47c4"))
                        }
                    }
                    it.description.isSoloKill -> {
                        setTitle("Honorable!", it.link.zkillboard)
                        setColor(Color.decode("#2b47c4"))
                    }
                    else -> {
                        setTitle("Kill checked!", it.link.zkillboard)
                        setColor(Color.decode("#7d000f"))
                    }
                }
                val finalBlow = it.attacker.filter { it.isFinalBlow }[0]
                setThumbnail("https://images.evetech.net/types/${it.victim.origin.shipType}/render")
                addField("Location:", "${it.location.solarSystem}", false)
                addField("Victim:", "${it.victim.getVictimShipAndName(false, it)}", false)
                if (it.description.isNpcKill) addField(
                    "Attackers(${it.attacker.size}):", "${
                        finalBlow.getShipAndName(
                            false, it
                        )
                    }", false
                )
                else addField(
                    "Attackers(${it.attacker.size}):", "${
                        finalBlow.getShipAndName(
                            false, it
                        )
                    }\nwith **${finalBlow.weapon ?: "Unknown weapon"}**", false
                )
                addField(
                    "", "Total ${it.value.total.compressIsk()} | Dropped ${it.value.dropped.compressIsk()}", false
                )
                setFooter(it.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            }.build())).queue()
        }
    }

    var tempChannel: TextChannel? = null
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "start" -> {
                tempChannel = event.channel.asTextChannel()
                event.reply("Killmail broadcasting started").queue()
            }

            "clr" -> {
                tempChannel = null
                event.reply("Killmail broadcasting stopped").queue()
            }
        }
    }
}