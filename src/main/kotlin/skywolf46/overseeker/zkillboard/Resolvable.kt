package skywolf46.overseeker.zkillboard

import kotlinx.coroutines.runBlocking
import skywolf46.overseeker.esi.EsiNameFetcher

interface Resolvable<T : Any> {
    fun resolveAll(): T {
        runBlocking {
            EsiNameFetcher.fetchNames(*getUnresolvedIds().toLongArray())
        }
        return resolve()
    }

    fun resolve(): T
    fun getUnresolvedIds(): List<Long>
}