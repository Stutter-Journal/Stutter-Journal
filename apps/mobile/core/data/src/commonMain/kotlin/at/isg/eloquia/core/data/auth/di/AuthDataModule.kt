package at.isg.eloquia.core.data.auth.di

import at.isg.eloquia.core.data.auth.remote.AuthApi
import at.isg.eloquia.core.data.auth.repository.AuthRepositoryImpl
import at.isg.eloquia.core.domain.auth.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val authDataModule: Module = module {
    single { AuthApi(get()) }
    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
        )
    }
}
