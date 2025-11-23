package at.isg.eloquia.core.data.entries.repository

import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.mapper.toDomain
import at.isg.eloquia.core.data.entries.mapper.toDto
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class JournalEntryRepositoryImpl(
    private val localDataSource: JournalEntryLocalDataSource,
) : JournalEntryRepository {

    override fun observeEntries(): Flow<List<JournalEntry>> =
        localDataSource.observeEntries().map { list -> list.map { it.toDomain() } }

    override suspend fun createEntry(entry: JournalEntry) {
        localDataSource.upsert(entry.toDto())
    }
}
