package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.data.auth.di.authDataModule
import at.isg.eloquia.core.data.entries.di.entriesDataModule
import at.isg.eloquia.core.network.ktor.di.networkKtorModule
import at.isg.eloquia.core.network.ktor.prefs.IosKeyValueStore
import at.isg.eloquia.core.network.ktor.prefs.KeyValueStore
import org.koin.core.module.Module
import org.koin.dsl.module

private const val BASE_URL = "http://api.eloquia.test/"

actual fun platformModules(): List<Module> = listOf(
    module {
        single<KeyValueStore> { IosKeyValueStore() }
    },
    networkKtorModule(baseUrl = BASE_URL, enableLogging = false),
    entriesDataModule,
    authDataModule,
)
