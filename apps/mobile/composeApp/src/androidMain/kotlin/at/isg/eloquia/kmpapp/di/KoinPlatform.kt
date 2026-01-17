package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.BuildConfig
import at.isg.eloquia.core.data.auth.di.authDataModule
import at.isg.eloquia.core.data.entries.di.entriesDataModule
import at.isg.eloquia.core.network.ktor.di.networkKtorModule
import at.isg.eloquia.core.network.ktor.prefs.AndroidKeyValueStore
import at.isg.eloquia.core.network.ktor.prefs.KeyValueStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModules(): List<Module> = listOf(
    module {
        single<KeyValueStore> { AndroidKeyValueStore(context = androidContext(), name = "eloquia_prefs") }
    },
    networkKtorModule(baseUrl = BuildConfig.BASE_URL, enableLogging = BuildConfig.DEBUG),
    entriesDataModule,
    authDataModule,
)
