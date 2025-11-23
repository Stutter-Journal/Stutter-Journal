package at.isg.eloquia.core.data.entries.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY created_at DESC")
    fun observeEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY created_at DESC")
    suspend fun getEntries(): List<JournalEntryEntity>

    @Upsert
    suspend fun upsert(entry: JournalEntryEntity)
}
