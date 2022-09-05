package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.esi.EsiNameFetcher
import skywolf46.overseeker.zkillboard.raw.KillLocation

class ResolvedKillLocation(origin: KillLocation) {
    val solarSystem = EsiNameFetcher.getName(origin.solarSystemId)
    val x = origin.x
    val y = origin.y
    val z = origin.z
}