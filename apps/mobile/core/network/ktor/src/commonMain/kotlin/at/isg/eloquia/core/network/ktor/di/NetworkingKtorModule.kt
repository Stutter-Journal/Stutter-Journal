package at.isg.eloquia.core.network.ktor.di

import at.isg.eloquia.core.domain.auth.repository.AuthSessionStore
import at.isg.eloquia.core.network.ktor.NetworkClient
import at.isg.eloquia.core.network.ktor.cookies.PersistentCookiesStorage
import at.isg.eloquia.core.network.ktor.createHttpClient
import at.isg.eloquia.core.network.ktor.prefs.KeyValueStore
import at.isg.eloquia.core.network.ktor.session.KtorAuthSessionStore
import org.koin.core.module.Module
import org.koin.dsl.module

fun networkKtorModule(
    baseUrl: String,
    enableLogging: Boolean = false,
): Module = module {
    single { PersistentCookiesStorage(prefs = get<KeyValueStore>()) }
    single<AuthSessionStore> { KtorAuthSessionStore(prefs = get(), cookiesStorage = get()) }
    single {
        createHttpClient(
            baseUrl = baseUrl,
            enableLogging = enableLogging,
            cookiesStorage = get<PersistentCookiesStorage>(),
        )
    }
    single { NetworkClient(get()) }
}
