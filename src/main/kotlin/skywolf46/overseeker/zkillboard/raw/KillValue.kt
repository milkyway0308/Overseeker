package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.at
import skywolf46.overseeker.util.of

class KillValue(data: JSONObject) {
    val total: Long = data at "zkb" of "totalValue"
    val dropped: Long = data at "zkb" of "droppedValue"
    val destroyed: Long = data at "zkb" of "destroyedValue"
    val fitted: Long = data at "zkb" of "droppedValue"
}