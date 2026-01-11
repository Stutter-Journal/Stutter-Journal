package at.isg.eloquia.kmpapp.logging

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

private var isLoggingInitialized: Boolean = false

actual fun initLogging(isDebug: Boolean) {
    if (isLoggingInitialized) return
    isLoggingInitialized = true

    Napier.base(DebugAntilog())
}
