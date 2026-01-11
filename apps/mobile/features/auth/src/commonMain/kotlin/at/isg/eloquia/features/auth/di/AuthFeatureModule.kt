package at.isg.eloquia.features.auth.di

import at.isg.eloquia.features.auth.presentation.link.LinkRequestViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule: Module = module {
    viewModelOf(::LinkRequestViewModel)
}
