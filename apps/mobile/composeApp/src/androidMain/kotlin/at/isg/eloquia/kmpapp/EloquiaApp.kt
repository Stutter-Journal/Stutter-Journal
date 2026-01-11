package at.isg.eloquia.kmpapp

import android.app.Application
import at.isg.eloquia.kmpapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class EloquiaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@EloquiaApp)
        }
    }
}
