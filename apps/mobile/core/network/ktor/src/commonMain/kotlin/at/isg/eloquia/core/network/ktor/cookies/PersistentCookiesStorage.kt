package at.isg.eloquia.core.network.ktor.cookies

import at.isg.eloquia.core.network.ktor.prefs.KeyValueStore
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// TODO: dafuq, what the mutex for, this is absolutely unnecessary...
class PersistentCookiesStorage(
    private val prefs: KeyValueStore,
    private val rememberMeKey: String = "auth.rememberMe.enabled",
    private val cookiesKey: String = "auth.cookies.json",
) : CookiesStorage {

    private val mutex = Mutex()
    private var loaded = false
    private var cookies: MutableList<Cookie> = mutableListOf()

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    suspend fun isRememberMeEnabled(): Boolean = prefs.getBoolean(rememberMeKey, false)

    suspend fun setRememberMeEnabled(enabled: Boolean) {
        prefs.putBoolean(rememberMeKey, enabled)
        if (!enabled) {
            // Only clear persisted cookies; keep in-memory session for this app run.
            prefs.remove(cookiesKey)
        }
    }

    /** Clears both persisted cookies and in-memory cookies. Use when session is invalid or on logout. */
    suspend fun clearAll() {
        mutex.withLock {
            cookies.clear()
            loaded = true
            prefs.remove(cookiesKey)
            prefs.putBoolean(rememberMeKey, false)
        }
    }

    private suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            loaded = true

            val enabled = prefs.getBoolean(rememberMeKey, false)
            if (!enabled) {
                cookies = mutableListOf()
                return
            }

            val raw = prefs.getString(cookiesKey).orEmpty()
            if (raw.isBlank()) {
                cookies = mutableListOf()
                return
            }

            val decoded = runCatching { json.decodeFromString<List<CookieDto>>(raw) }.getOrNull().orEmpty()
            cookies = decoded.mapNotNull { it.toCookieOrNull() }.toMutableList()
        }
    }

    private fun persistIfEnabled() {
        val enabled = prefs.getBoolean(rememberMeKey, false)
        if (!enabled) return

        val encoded = json.encodeToString(cookies.map { it.toDto() })
        prefs.putString(cookiesKey, encoded)
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        ensureLoaded()
        return mutex.withLock {
            val now = GMTDate()
            cookies
                .asSequence()
                .filter { !it.isExpired(now) }
                .filter { it.matches(requestUrl) }
                .toList()
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        ensureLoaded()
        mutex.withLock {
            cookies.removeAll { it.sameKeyAs(cookie) }
            if (!cookie.isExpired(GMTDate())) {
                cookies.add(cookie)
            }
            persistIfEnabled()
        }
    }

    override fun close() {
        // no-op
    }
}

@Serializable
private data class CookieDto(
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
    @SerialName("domain") val domain: String? = null,
    @SerialName("path") val path: String? = null,
    @SerialName("expiresEpochMillis") val expiresEpochMillis: Long? = null,
    @SerialName("maxAge") val maxAge: Int? = null,
    @SerialName("secure") val secure: Boolean = false,
    @SerialName("httpOnly") val httpOnly: Boolean = false,
)

private fun Cookie.toDto(): CookieDto = CookieDto(
    name = name,
    value = value,
    domain = domain,
    path = path,
    expiresEpochMillis = expires?.timestamp,
    maxAge = maxAge,
    secure = secure,
    httpOnly = httpOnly,
)

private fun CookieDto.toCookieOrNull(): Cookie? {
    val expires = expiresEpochMillis?.let { GMTDate(it) }
    return Cookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expires = expires,
        maxAge = maxAge,
        secure = secure,
        httpOnly = httpOnly,
    )
}

private fun Cookie.sameKeyAs(other: Cookie): Boolean = name == other.name && (domain ?: "") == (other.domain ?: "") && (path ?: "/") == (other.path ?: "/")

private fun Cookie.isExpired(now: GMTDate): Boolean {
    val exp = expires
    return exp != null && exp.timestamp <= now.timestamp
}

private fun Cookie.matches(url: Url): Boolean {
    val host = url.host
    val cookieDomain = (domain ?: host).trimStart('.')

    val domainMatches =
        host == cookieDomain || host.endsWith(".$cookieDomain")

    val cookiePath = path ?: "/"
    val pathMatches = url.encodedPath.startsWith(cookiePath)

    val secureMatches = !secure || url.protocol.name.equals("https", ignoreCase = true)

    return domainMatches && pathMatches && secureMatches
}
