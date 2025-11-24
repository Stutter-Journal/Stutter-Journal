package at.isg.eloquia.core.domain.entries.usecase

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class UpdateJournalEntryUseCase(
    private val repository: JournalEntryRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(request: UpdateJournalEntryRequest) {
        val updated = JournalEntry(
            id = request.entryId,
            title = request.title.trim(),
            content = request.content.trim(),
            createdAt = request.createdAt,
            updatedAt = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            tags = request.tags.map(String::trim).filter(String::isNotBlank),
        )
        repository.updateEntry(updated)
    }
}

data class UpdateJournalEntryRequest(
    val entryId: String,
    val title: String,
    val content: String,
    val createdAt: kotlinx.datetime.LocalDateTime,
    val tags: List<String>,
)
