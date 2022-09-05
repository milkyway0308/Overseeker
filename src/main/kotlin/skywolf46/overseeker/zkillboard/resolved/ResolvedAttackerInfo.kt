package skywolf46.overseeker.zkillboard.resolved

import skywolf46.overseeker.esi.EsiNameFetcher
import skywolf46.overseeker.zkillboard.raw.KillAttackerInfo
import skywolf46.overseeker.zkillboard.raw.KillCharacterInfo

class ResolvedAttackerInfo(info: KillAttackerInfo) : ResolvedCharacterInfo(info){
    val weapon = EsiNameFetcher.getName(info.weaponType)
    val isFinalBlow = info.isFinalBlow
}