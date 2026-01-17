package at.isg.eloquia.kmpapp.logging

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

private var isLoggingInitialized: Boolean = false

actual fun initLogging(isDebug: Boolean) {
    if (isLoggingInitialized) return
    isLoggingInitialized = true

    // DebugAntilog logs via println/System.out, which shows in Logcat as well.
    // You can swap to AndroidAntilog later if you prefer Log.* integration.
    Napier.base(DebugAntilog())
}
