package at.isg.eloquia.kmpapp.di

import at.isg.eloquia.core.domain.auth.di.authDomainModule
import at.isg.eloquia.core.domain.entries.di.entriesDomainModule
import at.isg.eloquia.features.auth.di.authFeatureModule
import at.isg.eloquia.features.entries.di.entriesFeatureModule
import at.isg.eloquia.features.progress.di.progressFeatureModule
import at.isg.eloquia.features.therapist.di.therapistFeatureModule
import at.isg.eloquia.kmpapp.logging.initLogging
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

typealias KoinAppDeclaration = KoinApplication.() -> Unit

/**
 * Convenience entrypoint used by iOS/Swift and Android.
 * Ensures logging is configured before any code tries to log.
 */
fun doInitKoin(
    isDebug: Boolean = false,
    appDeclaration: KoinAppDeclaration = {},
) {
    initLogging(isDebug)
    initKoin(appDeclaration)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            entriesDomainModule,
            authDomainModule,
            entriesFeatureModule,
            authFeatureModule,
            progressFeatureModule,
            therapistFeatureModule,
            kmpAppModule,
        )
        modules(platformModules())
    }
}

expect fun platformModules(): List<Module>
