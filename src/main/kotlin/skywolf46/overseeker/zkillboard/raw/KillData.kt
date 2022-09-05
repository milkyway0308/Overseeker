package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.at
import skywolf46.overseeker.util.fetch
import skywolf46.overseeker.util.list
import skywolf46.overseeker.util.of
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedKillData
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class KillData(data: JSONObject) : Resolvable<ResolvedKillData>{
    val recievedAt = System.currentTimeMillis()
    val location = KillLocation(data)
    val time: ZonedDateTime = Instant.parse(data of "killmail_time").atZone(ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneId.of("UTC+9"))
    val victim = KillVictimInfo(data at "victim")
    val attacker = data.list("attackers").fetch().map { KillAttackerInfo(it) }
    val link = KillLink(data)
    val value = KillValue(data)
    val description = KillBoardDescription(data)
    val processingTime = System.currentTimeMillis() - recievedAt

    override fun resolve(): ResolvedKillData {
        return ResolvedKillData(this)
    }

    override fun getUnresolvedIds(): List<Long> {
        val list = attacker.map { it.getUnresolvedIds() }.toMutableList()
        list.add(location.getUnresolvedIds())
        list.add(victim.getUnresolvedIds())
        return list.flatten()
    }
}
