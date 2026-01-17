package at.isg.eloquia.core.data.entries.di

import at.isg.eloquia.core.data.entries.remote.EntriesSyncApi
import at.isg.eloquia.core.data.entries.repository.JournalEntryRepositoryImpl
import at.isg.eloquia.core.data.sync.DataSyncRepositoryImpl
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import at.isg.eloquia.core.domain.sync.DataSyncRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val entriesDataModule: Module = module {
    includes(platformEntriesDataModule())

    single<JournalEntryRepository> { JournalEntryRepositoryImpl(get()) }

    single { EntriesSyncApi(get()) }
    single<DataSyncRepository> { DataSyncRepositoryImpl(local = get(), api = get()) }
}

expect fun platformEntriesDataModule(): Module
