package skywolf46.overseeker.util

import org.json.simple.JSONArray
import org.json.simple.JSONObject


internal infix fun JSONObject.at(key: String): JSONObject {
    return get(key) as JSONObject
}

internal infix fun <T : Any> JSONObject.of(key: String): T {
    return get(key) as T
}

internal infix fun <T : Any> JSONObject.maybe(key: String): T? {
    return get(key) as T?
}


internal infix fun JSONObject.list(key: String): JSONArray {
    return get(key) as JSONArray
}

internal fun JSONArray.fetch(): List<JSONObject> {
    return toList().map { x -> x as JSONObject }
}