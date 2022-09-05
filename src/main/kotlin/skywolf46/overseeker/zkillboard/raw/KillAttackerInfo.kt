package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.maybe
import skywolf46.overseeker.util.of
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedAttackerInfo

class KillAttackerInfo(data: JSONObject) : KillCharacterInfo(data), Resolvable<ResolvedAttackerInfo> {
    val weaponType: Long = (data maybe "weapon_type_id") ?: 0
    val damageDealt: Long = data of "damage_done"
    val isFinalBlow: Boolean = data of "final_blow"

    override fun resolve(): ResolvedAttackerInfo {
        return ResolvedAttackerInfo(this)
    }

    override fun getUnresolvedIds(): List<Long> {
        return super.getUnresolvedId() + weaponType
    }


}