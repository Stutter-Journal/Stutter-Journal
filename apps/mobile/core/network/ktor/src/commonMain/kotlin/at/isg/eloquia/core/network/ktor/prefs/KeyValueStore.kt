package at.isg.eloquia.core.network.ktor.prefs

interface KeyValueStore {
    fun getString(key: String): String?

    fun putString(key: String, value: String)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun putBoolean(key: String, value: Boolean)

    fun remove(key: String)
}
