package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.zkillboard.raw.KillData

class ResolvedKillData(origin: KillData) {
    val receivedAt = origin.recievedAt
    val location = origin.location.resolve()
    val time = origin.time
    val victim = origin.victim.resolve()
    val attacker = origin.attacker.map { it.resolve() }
    val link = origin.link
    val value = origin.value
    val description = origin.description
}