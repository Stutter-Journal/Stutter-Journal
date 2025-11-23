package at.isg.eloquia.features.entries.di

import at.isg.eloquia.features.entries.presentation.list.EntriesListViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

val entriesFeatureModule: Module = module {
    viewModelOf(::EntriesListViewModel)
}
