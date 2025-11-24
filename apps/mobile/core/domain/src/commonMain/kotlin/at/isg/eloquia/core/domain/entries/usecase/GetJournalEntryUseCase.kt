package at.isg.eloquia.core.domain.entries.usecase

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository

class GetJournalEntryUseCase(
    private val repository: JournalEntryRepository,
) {
    suspend operator fun invoke(id: String): JournalEntry? = repository.getEntry(id)
}
