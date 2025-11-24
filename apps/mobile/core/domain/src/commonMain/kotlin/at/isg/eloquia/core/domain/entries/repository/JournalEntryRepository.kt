package at.isg.eloquia.core.domain.entries.repository

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction representing the source of truth for journal entries.
 */
interface JournalEntryRepository {
    fun observeEntries(): Flow<List<JournalEntry>>

    suspend fun createEntry(entry: JournalEntry)

    suspend fun updateEntry(entry: JournalEntry)

    suspend fun deleteEntry(id: String)

    suspend fun getEntry(id: String): JournalEntry?
}
