package at.isg.eloquia.kmpapp

import android.app.Application
import at.isg.eloquia.BuildConfig
import at.isg.eloquia.kmpapp.di.initKoin
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import org.koin.android.ext.koin.androidContext

class EloquiaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Logger.setLogWriters(platformLogWriter())
        Logger.setMinSeverity(if (BuildConfig.DEBUG) Severity.Debug else Severity.Info)

        initKoin {
            androidContext(this@EloquiaApp)
        }
    }
}
