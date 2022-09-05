package skywolf46.overseeker.zkillboard.raw

import org.json.simple.JSONObject
import skywolf46.overseeker.util.maybe
import skywolf46.overseeker.zkillboard.Resolvable
import skywolf46.overseeker.zkillboard.resolved.ResolvedCharacterInfo

abstract class KillCharacterInfo(
    val characterId: Long,
    val corporationId: Long,
    val allianceId: Long,
    val shipType: Long,
)  {
    constructor(data: JSONObject) :
            this(
                (data maybe "character_id") ?: -1L,
                (data maybe "corporation_id") ?: -1L,
                (data maybe "alliance_id") ?: -1L,
                (data maybe "ship_type_id") ?: -1L
            )

    protected fun getUnresolvedId(): List<Long> {
        return listOf(
            characterId,
            corporationId,
            allianceId,
            shipType
        )
    }
}