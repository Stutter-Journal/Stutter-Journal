package at.isg.eloquia.core.data.entries.local

import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.db.JournalEntryDao
import at.isg.eloquia.core.data.entries.db.toDto
import at.isg.eloquia.core.data.entries.db.toEntity
import at.isg.eloquia.core.data.entries.dto.JournalEntryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomJournalEntryLocalDataSource(
    private val dao: JournalEntryDao,
) : JournalEntryLocalDataSource {

    override fun observeEntries(): Flow<List<JournalEntryDto>> = dao.observeEntries().map { list -> list.map { it.toDto() } }

    override suspend fun getEntries(): List<JournalEntryDto> = dao.getEntries().map { it.toDto() }

    override suspend fun getEntry(id: String): JournalEntryDto? = dao.getEntry(id)?.toDto()

    override suspend fun upsert(entry: JournalEntryDto) {
        dao.upsert(entry.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
