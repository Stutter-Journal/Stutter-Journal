package at.isg.eloquia.core.domain.auth.repository

/**
 * Stores whether the user wants to persist their session ("Remember me") and manages session persistence.
 *
 * The underlying auth mechanism in this app is cookie-based.
 */
interface AuthSessionStore {
    suspend fun isRememberMeEnabled(): Boolean

    /**
     * Enables/disables persistence for future app launches.
     * Disabling should clear any *persisted* session data, but should not log the user out for the current run.
     */
    suspend fun setRememberMeEnabled(enabled: Boolean)

    /** Clears persisted session data (used when Remember Me is disabled). */
    suspend fun clearRememberedSession()

    /** Clears persisted + in-memory session data (used when backend says the session is invalid or on logout). */
    suspend fun clearSession()
}
