package at.isg.eloquia.core.domain.logging

import co.touchlab.kermit.Logger
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val loggingModule: Module = module {
    // Bind the global Logger companion (which is also a Logger instance).
    single<Logger> { Logger }

    // Create tagged loggers via DI.
    // IMPORTANT: this is qualified so it doesn't conflict with the unqualified base Logger.
    factory<Logger>(named("tagged")) { (tag: String) -> get<Logger>().withTag(tag) }
}
