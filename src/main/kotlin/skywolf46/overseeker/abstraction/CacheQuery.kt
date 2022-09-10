package skywolf46.overseeker.abstraction

import kotlin.reflect.KClass

interface CacheQuery<T: Any> {
    fun constructTable(table: String, valueType: KClass<T>) : Boolean

    fun getNonExists(table: String, vararg key: String): List<String>

    fun cache(table: String, vararg data: Pair<String, T>)
}