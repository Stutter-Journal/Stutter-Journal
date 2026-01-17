package at.isg.eloquia.kmpapp.logging

/**
 * Initializes the app-wide logging backend.
 *
 * Napier is a no-op until [io.github.aakira.napier.Napier.base] is configured.
 */
expect fun initLogging(isDebug: Boolean)
