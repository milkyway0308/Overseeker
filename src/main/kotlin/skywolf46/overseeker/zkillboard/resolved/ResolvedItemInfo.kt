package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.esi.EsiNameFetcher
import skywolf46.overseeker.zkillboard.raw.KillItemInfo

class ResolvedItemInfo(origin: KillItemInfo) {
    val itemName = EsiNameFetcher.getName(origin.itemType)
}