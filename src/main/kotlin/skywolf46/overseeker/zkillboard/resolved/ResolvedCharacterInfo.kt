package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.esi.EsiNameFetcher
import skywolf46.overseeker.zkillboard.raw.KillCharacterInfo

open class ResolvedCharacterInfo(
    val origin: KillCharacterInfo
) {
    val character = EsiNameFetcher.getName(origin.characterId)
    val corporation = EsiNameFetcher.getName(origin.corporationId)
    val alliance = EsiNameFetcher.getName(origin.allianceId)
    val ship = EsiNameFetcher.getName(origin.shipType)

    fun asFullName(): String? {
        if (character == null) {
            if (ship != null) {
                if (corporation != null) {
                    return "$ship($corporation)"
                }
                return ship
            }
            if (corporation != null) {
                return corporation
            }
            if (alliance != null) {
                return alliance
            }
            return null
        }
        if (corporation != null) {
            return "$character($corporation)"
        }
        return character
    }

    fun getCharacterKillBoard(): String {
        return "https://zkillboard.com/character/${origin.characterId}/"
    }

    fun asFullLinkedName(): String {
        if (corporation != null) {
            return "[$character](${getCharacterKillBoard()}) ($corporation)"
        }
        return "[$character](${getCharacterKillBoard()})"
    }

    fun getVictimShipAndName(ignoreNpcCondition: Boolean, info: ResolvedKillData): String? {
        if (!ignoreNpcCondition && info.victim.origin.characterId == -1L)
            return if (corporation == null) ship else "$ship ($corporation)"
        return "**$ship** by ${asFullLinkedName()}"
    }

    fun getShipAndName(ignoreNpcCondition: Boolean, info: ResolvedKillData): String? {
        if (origin.characterId == -1L)
            return if (corporation == null) ship else "$ship ($corporation)"
        return "**$ship** by ${asFullLinkedName()}"
    }
}