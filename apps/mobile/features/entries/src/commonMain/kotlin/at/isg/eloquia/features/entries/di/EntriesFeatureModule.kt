package at.isg.eloquia.features.entries.di

import at.isg.eloquia.features.entries.presentation.list.EntriesListViewModel
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val entriesFeatureModule: Module = module {
    viewModelOf(::EntriesListViewModel)
    viewModelOf(::NewEntryViewModel)
}
