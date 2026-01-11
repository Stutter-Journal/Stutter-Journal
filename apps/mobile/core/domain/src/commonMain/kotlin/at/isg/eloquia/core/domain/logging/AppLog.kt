package at.isg.eloquia.core.domain.logging

import io.github.aakira.napier.Napier

/**
 * Simple multiplatform logger.
 */
object AppLog {
    fun d(tag: String, message: String) {
        Napier.d(message = message, tag = tag)
    }

    fun i(tag: String, message: String) {
        Napier.i(message = message, tag = tag)
    }

    fun w(tag: String, message: String) {
        Napier.w(message = message, tag = tag)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Napier.e(message = message, throwable = throwable, tag = tag)
    }
}
