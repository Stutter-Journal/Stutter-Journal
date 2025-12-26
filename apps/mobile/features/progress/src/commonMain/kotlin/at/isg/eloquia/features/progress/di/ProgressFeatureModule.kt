package at.isg.eloquia.features.progress.di

import at.isg.eloquia.features.progress.presentation.ProgressViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val progressFeatureModule: Module = module {
    viewModelOf(::ProgressViewModel)
}
