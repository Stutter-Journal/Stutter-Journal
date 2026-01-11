package at.isg.eloquia.core.domain.logging

import co.touchlab.kermit.Logger

actual object AppLog {
    actual fun d(tag: String, message: String) {
        Logger.d(messageString = message, tag = tag)
    }

    actual fun i(tag: String, message: String) {
        Logger.i(messageString = message, tag = tag)
    }

    actual fun w(tag: String, message: String) {
        Logger.w(messageString = message, tag = tag)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        Logger.e(messageString = message, throwable = throwable, tag = tag)
    }
}
