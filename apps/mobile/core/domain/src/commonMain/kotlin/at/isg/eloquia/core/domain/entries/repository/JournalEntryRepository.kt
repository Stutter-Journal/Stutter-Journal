package at.isg.eloquia.core.domain.entries.repository

import at.isg.eloquia.core.domain.entries.model.JournalEntry

/**
 * Abstraction representing the source of truth for journal entries.
 */
interface JournalEntryRepository {
    fun observeEntries(): Flow<List<JournalEntry>>
    suspend fun createEntry(entry: JournalEntry)
}
