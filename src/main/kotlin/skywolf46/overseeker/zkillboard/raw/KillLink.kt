package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.at
import skywolf46.overseeker.util.of

class KillLink(data: JSONObject) {
    val zkillboard: String = data.at("zkb").of<String>("url").replace("\\", "")
    val esi: String = data.at("zkb").of<String>("esi").replace("\\", "")
}