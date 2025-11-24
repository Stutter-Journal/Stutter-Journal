package at.isg.eloquia.features.entries.di

import at.isg.eloquia.features.entries.presentation.detail.EntryDetailViewModel
import at.isg.eloquia.features.entries.presentation.list.EntriesListViewModel
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val entriesFeatureModule: Module = module {
    viewModelOf(::EntriesListViewModel)
    viewModel { (entryId: String?) ->
        NewEntryViewModel(
            createJournalEntryUseCase = get(),
            updateJournalEntryUseCase = get(),
            getJournalEntryUseCase = get(),
            clock = get(),
            initialEntryId = entryId,
        )
    }
    viewModel { (entryId: String) ->
        EntryDetailViewModel(
            entryId = entryId,
            observeJournalEntriesUseCase = get(),
            deleteJournalEntryUseCase = get(),
        )
    }
}
