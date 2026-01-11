package at.isg.eloquia.core.domain.logging

/**
 * Simple multiplatform logger.
 */
expect object AppLog {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
