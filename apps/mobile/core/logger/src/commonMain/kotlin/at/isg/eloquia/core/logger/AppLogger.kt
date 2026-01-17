package at.isg.eloquia.core.logger

expect object AppLogger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
}

fun AppLogger.w(tag: String, message: String) {
    i(tag, "WARN: $message")
}
