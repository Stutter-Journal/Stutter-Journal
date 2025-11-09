package at.isg.eloquia.kmpapp

import android.app.Application
import at.isg.eloquia.kmpapp.di.initKoin

class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
