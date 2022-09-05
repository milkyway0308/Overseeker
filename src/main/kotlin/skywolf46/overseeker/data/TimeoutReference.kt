package skywolf46.overseeker.data

class TimeoutReference<T : Any>(private val reference: T) {
    companion object {
        private const val TIME_OUT_THRESHOLD = 7_200_000L;

    }
    private var time = System.currentTimeMillis()

    operator fun invoke(): T {
        time = System.currentTimeMillis()
        return reference
    }

    fun isTimedOut() : Boolean {
        return (System.currentTimeMillis() - time) >= TIME_OUT_THRESHOLD
    }

}