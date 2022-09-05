package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.at
import skywolf46.overseeker.util.of
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedKillLocation

class KillLocation(data: JSONObject) : Resolvable<ResolvedKillLocation>{
    val solarSystemId: Long = data of "solar_system_id"
    val locationId: Long = data at "zkb" of "locationID"
    val x: Double = data at "victim" at "position" of "x"
    val y: Double = data at "victim" at "position" of "y"
    val z: Double = data at "victim" at "position" of "z"

    override fun resolve(): ResolvedKillLocation {
        return ResolvedKillLocation(this)
    }

    override fun getUnresolvedIds(): List<Long> {
        return listOf(solarSystemId)
    }
}
