package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.fetch
import skywolf46.overseeker.util.list
import skywolf46.overseeker.util.of
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedVictimInfo

class KillVictimInfo(data: JSONObject) : KillCharacterInfo(data), Resolvable<ResolvedVictimInfo> {
    val totalDamageTaken: Long = data of "damage_taken"
    val items = data.list("items").fetch().map { KillItemInfo(it) }

    override fun resolve(): ResolvedVictimInfo {
        return ResolvedVictimInfo(this)
    }

    override fun getUnresolvedIds(): List<Long> {
        val type = items.map { it.getUnresolvedIds() }.toMutableList()
        type.add(super.getUnresolvedId())
        return type.flatten()
    }
}