package at.isg.eloquia.core.domain.logging

import at.isg.eloquia.core.logger.AppLogger
import at.isg.eloquia.core.logger.w

actual object AppLog {
    actual fun d(tag: String, message: String) {
        AppLogger.d(tag, message)
    }

    actual fun i(tag: String, message: String) {
        AppLogger.i(tag, message)
    }

    actual fun w(tag: String, message: String) {
        AppLogger.w(tag, message)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        AppLogger.e(tag, message, throwable)
    }
}
