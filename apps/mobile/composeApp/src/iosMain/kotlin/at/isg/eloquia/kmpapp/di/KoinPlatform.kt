package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.data.auth.di.authDataModule
import at.isg.eloquia.core.data.entries.di.entriesDataModule
import at.isg.eloquia.core.network.ktor.di.networkKtorModule
import org.koin.core.module.Module

private const val BASE_URL = "http://10.0.2.2:8080/"

actual fun platformModules(): List<Module> = listOf(
	networkKtorModule(baseUrl = BASE_URL, enableLogging = false),
	entriesDataModule,
	authDataModule,
)
