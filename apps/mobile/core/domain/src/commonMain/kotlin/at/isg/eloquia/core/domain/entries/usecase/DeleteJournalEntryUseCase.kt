package at.isg.eloquia.core.domain.entries.usecase

import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository

class DeleteJournalEntryUseCase(
    private val repository: JournalEntryRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.deleteEntry(id)
    }
}
