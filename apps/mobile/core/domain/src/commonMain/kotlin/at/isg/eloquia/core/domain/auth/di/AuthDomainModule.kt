package at.isg.eloquia.core.domain.auth.di

import at.isg.eloquia.core.domain.auth.usecase.RequestLinkUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val authDomainModule: Module = module {
    factory { RequestLinkUseCase(get()) }
}
