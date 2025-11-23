package at.isg.eloquia.core.domain.util

/**
 * Platform-specific entry id generator to avoid JVM-only UUID usage in common code.
 */
expect fun generateEntryId(): String
