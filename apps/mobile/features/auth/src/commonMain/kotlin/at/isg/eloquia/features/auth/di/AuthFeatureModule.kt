package at.isg.eloquia.features.auth.di

import at.isg.eloquia.features.auth.presentation.landing.AuthLandingViewModel
import at.isg.eloquia.features.auth.presentation.link.LinkRequestViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule: Module = module {
    viewModel {
        AuthLandingViewModel(
            patientLogin = get(),
            patientRegister = get(),
        )
    }
    viewModelOf(::LinkRequestViewModel)
}
