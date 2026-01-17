package at.isg.eloquia.core.network.ktor.session

import at.isg.eloquia.core.domain.auth.repository.AuthSessionStore
import at.isg.eloquia.core.network.ktor.cookies.PersistentCookiesStorage
import at.isg.eloquia.core.network.ktor.prefs.KeyValueStore

class KtorAuthSessionStore(
    private val prefs: KeyValueStore,
    private val cookiesStorage: PersistentCookiesStorage,
) : AuthSessionStore {

    private val rememberMeKey: String = "auth.rememberMe.enabled"
    private val cookiesKey: String = "auth.cookies.json"

    override suspend fun isRememberMeEnabled(): Boolean = prefs.getBoolean(rememberMeKey, false)

    override suspend fun setRememberMeEnabled(enabled: Boolean) {
        // Just flip the flag. Persistence happens in the cookie storage.
        prefs.putBoolean(rememberMeKey, enabled)
    }

    override suspend fun clearRememberedSession() {
        prefs.remove(cookiesKey)
    }

    override suspend fun clearSession() {
        cookiesStorage.clearAll()
    }
}
