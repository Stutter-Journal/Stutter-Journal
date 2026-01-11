package at.isg.eloquia.kmpapp

import android.app.Application
import at.isg.eloquia.BuildConfig
import at.isg.eloquia.kmpapp.di.doInitKoin
import org.koin.android.ext.koin.androidContext

class EloquiaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // Make slf4j-simple output visible in Logcat.
            System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info")
        }

        doInitKoin(isDebug = BuildConfig.DEBUG) {
            androidContext(this@EloquiaApp)
        }
    }
}
