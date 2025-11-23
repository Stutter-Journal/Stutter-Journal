package at.isg.eloquia.core.data.entries.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [JournalEntryEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(JournalEntryTypeConverters::class)
internal abstract class EloquiaDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): JournalEntryDao
}
