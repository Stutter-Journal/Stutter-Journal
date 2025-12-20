package at.isg.eloquia.features.entries.di

import at.isg.eloquia.features.entries.presentation.list.EntriesListViewModel
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val entriesFeatureModule: Module = module {
    viewModel {
        EntriesListViewModel(
            observeEntriesUseCase = get(),
            createJournalEntryUseCase = get(),
            deleteJournalEntryUseCase = get(),
            clock = get(),
        )
    }

    viewModel { (entryId: String?) ->
        NewEntryViewModel(
            createJournalEntryUseCase = get(),
            updateJournalEntryUseCase = get(),
            getJournalEntryUseCase = get(),
            clock = get(),
            initialEntryId = entryId,
        )
    }
}
