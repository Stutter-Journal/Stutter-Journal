package at.isg.eloquia.core.data.entries.di

import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.local.InMemoryJournalEntryLocalDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformEntriesDataModule(): Module = module {
    single<JournalEntryLocalDataSource> { InMemoryJournalEntryLocalDataSource() }
}
