package at.isg.eloquia.core.data.auth.di

import at.isg.eloquia.core.data.auth.remote.AuthApi
import at.isg.eloquia.core.data.auth.repository.AuthRepositoryImpl
import at.isg.eloquia.core.domain.auth.repository.AuthRepository
import co.touchlab.kermit.Logger
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authDataModule: Module = module {
    single { AuthApi(get()) }
    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            logger = get<Logger>(named("tagged")) { parametersOf("Auth") },
        )
    }
}
