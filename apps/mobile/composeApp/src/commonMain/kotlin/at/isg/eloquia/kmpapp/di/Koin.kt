package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.domain.entries.di.entriesDomainModule
import at.isg.eloquia.features.entries.di.entriesFeatureModule
import at.isg.eloquia.features.progress.di.progressFeatureModule
import at.isg.eloquia.kmpapp.data.InMemoryMuseumStorage
import at.isg.eloquia.kmpapp.data.KtorMuseumApi
import at.isg.eloquia.kmpapp.data.MuseumApi
import at.isg.eloquia.kmpapp.data.MuseumRepository
import at.isg.eloquia.kmpapp.data.MuseumStorage
import at.isg.eloquia.kmpapp.presentation.detail.DetailViewModel
import at.isg.eloquia.kmpapp.presentation.list.ListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                // TODO Fix API so it serves application/json
                json(json, contentType = ContentType.Any)
            }
        }
    }

    single<MuseumApi> { KtorMuseumApi(get()) }
    single<MuseumStorage> { InMemoryMuseumStorage() }
    single {
        MuseumRepository(get(), get()).apply {
            initialize()
        }
    }
}

val viewModelModule = module {
    factoryOf(::ListViewModel)
    factoryOf(::DetailViewModel)
}

typealias KoinAppDeclaration = KoinApplication.() -> Unit

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            dataModule,
            viewModelModule,
            entriesDomainModule,
            entriesFeatureModule,
            progressFeatureModule,
        )
        modules(platformModules())
    }
}

expect fun platformModules(): List<Module>
