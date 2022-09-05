package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.zkillboard.raw.KillVictimInfo

class ResolvedVictimInfo(origin: KillVictimInfo) : ResolvedCharacterInfo(origin) {
    val totalDamageTaken = origin.totalDamageTaken
    val items = origin.items
}