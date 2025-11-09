package at.isg.eloquia

import android.app.Application
import at.isg.eloquia.di.initKoin

class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
