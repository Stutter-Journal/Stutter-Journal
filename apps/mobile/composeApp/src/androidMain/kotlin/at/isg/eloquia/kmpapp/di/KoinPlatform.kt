package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.BuildConfig
import at.isg.eloquia.core.data.auth.di.authDataModule
import at.isg.eloquia.core.data.entries.di.entriesDataModule
import at.isg.eloquia.core.network.ktor.di.networkKtorModule
import org.koin.core.module.Module

actual fun platformModules(): List<Module> = listOf(
	networkKtorModule(baseUrl = BuildConfig.BASE_URL, enableLogging = BuildConfig.DEBUG),
	entriesDataModule,
	authDataModule,
)
