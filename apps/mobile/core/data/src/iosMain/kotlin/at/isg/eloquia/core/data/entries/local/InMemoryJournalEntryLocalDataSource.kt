package at.isg.eloquia.core.data.entries.local

import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.dto.JournalEntryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class InMemoryJournalEntryLocalDataSource : JournalEntryLocalDataSource {
    private val entries = MutableStateFlow<List<JournalEntryDto>>(emptyList())

    override fun observeEntries(): Flow<List<JournalEntryDto>> = entries.asStateFlow()

    override suspend fun getEntries(): List<JournalEntryDto> = entries.value

    override suspend fun upsert(entry: JournalEntryDto) {
        entries.value = listOf(entry) + entries.value.filterNot { it.id == entry.id }
    }
}
