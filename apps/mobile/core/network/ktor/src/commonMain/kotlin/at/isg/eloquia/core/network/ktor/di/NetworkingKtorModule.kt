package at.isg.eloquia.core.network.ktor.di

import at.isg.eloquia.core.network.ktor.NetworkClient
import at.isg.eloquia.core.network.ktor.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun networkKtorModule(
    baseUrl: String,
    enableLogging: Boolean = false,
): Module = module {
    single { createHttpClient(baseUrl = baseUrl, enableLogging = enableLogging) }
    single { NetworkClient(get()) }
}
