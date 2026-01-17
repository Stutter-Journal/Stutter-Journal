package at.isg.eloquia.core.domain.auth.di

import at.isg.eloquia.core.domain.auth.usecase.ClearSessionUseCase
import at.isg.eloquia.core.domain.auth.usecase.GetRememberMeEnabledUseCase
import at.isg.eloquia.core.domain.auth.usecase.PatientLoginUseCase
import at.isg.eloquia.core.domain.auth.usecase.PatientMeUseCase
import at.isg.eloquia.core.domain.auth.usecase.PatientRegisterUseCase
import at.isg.eloquia.core.domain.auth.usecase.RedeemPairingCodeUseCase
import at.isg.eloquia.core.domain.auth.usecase.RequestLinkUseCase
import at.isg.eloquia.core.domain.auth.usecase.RevokeLinksUseCase
import at.isg.eloquia.core.domain.auth.usecase.SetRememberMeEnabledUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val authDomainModule: Module = module {
    factory { RequestLinkUseCase(get()) }
    factory { RedeemPairingCodeUseCase(get()) }
    factory { RevokeLinksUseCase(get()) }
    factory { PatientLoginUseCase(get()) }
    factory { PatientRegisterUseCase(get()) }
    factory { PatientMeUseCase(get()) }

    factory { GetRememberMeEnabledUseCase(get()) }
    factory { SetRememberMeEnabledUseCase(get()) }
    factory { ClearSessionUseCase(get()) }
}
