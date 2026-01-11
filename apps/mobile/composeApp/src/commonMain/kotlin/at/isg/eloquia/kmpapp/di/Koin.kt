package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.domain.auth.di.authDomainModule
import at.isg.eloquia.core.domain.entries.di.entriesDomainModule
import at.isg.eloquia.core.domain.logging.loggingModule
import at.isg.eloquia.features.auth.di.authFeatureModule
import at.isg.eloquia.features.entries.di.entriesFeatureModule
import at.isg.eloquia.features.progress.di.progressFeatureModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

typealias KoinAppDeclaration = KoinApplication.() -> Unit

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            loggingModule,
            entriesDomainModule,
            authDomainModule,
            entriesFeatureModule,
            authFeatureModule,
            progressFeatureModule,
        )
        modules(platformModules())
    }
}

expect fun platformModules(): List<Module>
