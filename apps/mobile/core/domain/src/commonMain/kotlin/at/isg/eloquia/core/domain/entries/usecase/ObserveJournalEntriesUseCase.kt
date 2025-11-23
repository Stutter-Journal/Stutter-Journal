package at.isg.eloquia.core.domain.entries.usecase

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow

class ObserveJournalEntriesUseCase(
    private val repository: JournalEntryRepository,
) {
    operator fun invoke(): Flow<List<JournalEntry>> = repository.observeEntries()
}
