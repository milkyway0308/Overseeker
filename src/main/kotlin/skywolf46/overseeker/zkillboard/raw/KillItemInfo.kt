package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.maybe
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedItemInfo

class KillItemInfo(data: JSONObject) : Resolvable<ResolvedItemInfo> {
    val itemType: Long = (data maybe "item_type_id") ?: -1L
    val flag: Long = (data maybe "flag") ?: 0L
    val destroyedAmount: Long = (data maybe "quantity_destroyed") ?: 0L
    val isSingleton: Boolean = data.maybe<Long>("singleton") != 0L

    override fun resolve(): ResolvedItemInfo {
        return ResolvedItemInfo(this)
    }

    override fun getUnresolvedIds(): List<Long> {
        return listOf(itemType)
    }
}