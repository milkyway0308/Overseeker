package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.at
import skywolf46.overseeker.util.of

class KillBoardDescription(data: JSONObject) {
    val isAwoxed: Boolean = data at "zkb" of "awox"
    val isSoloKill: Boolean = data at "zkb" of "solo"
    val isNpcKill: Boolean = data at "zkb" of "npc"
}