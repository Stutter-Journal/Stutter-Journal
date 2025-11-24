package at.isg.eloquia.core.data.entries.di

import androidx.room.Room
import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.db.EloquiaDatabase
import at.isg.eloquia.core.data.entries.local.RoomJournalEntryLocalDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformEntriesDataModule(): Module = module {
    single<EloquiaDatabase> {
        Room.databaseBuilder(
            androidContext(),
            EloquiaDatabase::class.java,
            "eloquia.db",
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<EloquiaDatabase>().journalEntryDao() }

    single<JournalEntryLocalDataSource> { RoomJournalEntryLocalDataSource(get()) }
}
