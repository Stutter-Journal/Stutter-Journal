package at.isg.eloquia.core.network.ktor.prefs

import platform.Foundation.NSUserDefaults

class IosKeyValueStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : KeyValueStore {
    override fun getString(key: String): String? = userDefaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // NSUserDefaults doesn't have a 'contains', so store default explicitly when missing.
        return if (userDefaults.objectForKey(key) == null) defaultValue else userDefaults.boolForKey(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, forKey = key)
    }

    override fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
    }
}
