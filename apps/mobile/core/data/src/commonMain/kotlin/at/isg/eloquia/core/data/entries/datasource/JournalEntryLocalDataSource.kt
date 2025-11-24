package at.isg.eloquia.core.data.entries.datasource

import at.isg.eloquia.core.data.entries.dto.JournalEntryDto
import kotlinx.coroutines.flow.Flow

interface JournalEntryLocalDataSource {
    fun observeEntries(): Flow<List<JournalEntryDto>>
    suspend fun getEntries(): List<JournalEntryDto>
    suspend fun getEntry(id: String): JournalEntryDto?
    suspend fun upsert(entry: JournalEntryDto)
    suspend fun delete(id: String)
}
