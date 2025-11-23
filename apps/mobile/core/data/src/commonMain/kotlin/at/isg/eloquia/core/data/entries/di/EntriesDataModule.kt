package at.isg.eloquia.core.data.entries.di

import at.isg.eloquia.core.data.entries.repository.JournalEntryRepositoryImpl
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val entriesDataModule: Module = module {
    includes(platformEntriesDataModule())

    single<JournalEntryRepository> { JournalEntryRepositoryImpl(get()) }
}

expect fun platformEntriesDataModule(): Module
