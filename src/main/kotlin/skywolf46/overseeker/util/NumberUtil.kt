package skywolf46.overseeker.util

fun Long.compressIsk(): String {
    if (this >= 1000L) {
        return toDouble().compressKilo()
    }
    return "$this ISK"
}

private fun Double.compressKilo(): String {
    val compressed = this / 1000.0
    if (compressed >= 1000) {
        return compressed.compressMillion()
    }
    return String.format("%.1fK ISK", compressed)
}

private fun Double.compressMillion(): String {
    val compressed = this / 1000.0
    if (compressed >= 1000) {
        return compressed.compressBillion()
    }
    return String.format("%.1fM ISK", compressed)
}

private fun Double.compressBillion(): String {
    val compressed = this / 1000.0
    if (compressed >= 1000) {
        return compressed.compressTrillion()
    }
    if (compressed >= 10) {
        return String.format("__**%.2fB ISK**__", compressed)
    }
    if (compressed >= 5) {
        return String.format("**%.2fB ISK**", compressed)
    }
    if (compressed >= 1) {
        return String.format("__%.2fB ISK__", compressed)
    }
    return String.format("%.2fB ISK", compressed)
}

private fun Double.compressTrillion(): String {
    val compressed = this / 1000.0
    return String.format("__**%.3fT ISK**__", compressed)
}

