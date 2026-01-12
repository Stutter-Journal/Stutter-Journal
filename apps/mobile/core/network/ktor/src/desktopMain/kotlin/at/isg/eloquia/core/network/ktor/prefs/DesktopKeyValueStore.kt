package at.isg.eloquia.core.network.ktor.prefs

import java.util.prefs.Preferences

class DesktopKeyValueStore(
    nodeName: String,
) : KeyValueStore {
    private val prefs = Preferences.userRoot().node(nodeName)

    override fun getString(key: String): String? = prefs.get(key, null)

    override fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    override fun remove(key: String) {
        prefs.remove(key)
    }
}
