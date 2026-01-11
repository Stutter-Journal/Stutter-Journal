package at.isg.eloquia.features.auth.di

import at.isg.eloquia.features.auth.presentation.landing.AuthLandingViewModel
import at.isg.eloquia.features.auth.presentation.link.LinkRequestViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.parameter.parametersOf

val authFeatureModule: Module = module {
    viewModel {
        AuthLandingViewModel(
            patientLogin = get(),
            patientRegister = get(),
            logger = get(named("tagged")) { parametersOf("Auth") },
        )
    }
    viewModelOf(::LinkRequestViewModel)
}
