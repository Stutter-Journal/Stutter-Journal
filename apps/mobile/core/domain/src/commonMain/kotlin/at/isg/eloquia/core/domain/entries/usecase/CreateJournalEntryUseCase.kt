package at.isg.eloquia.core.domain.entries.usecase

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.repository.JournalEntryRepository
import at.isg.eloquia.core.domain.util.generateEntryId
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class CreateJournalEntryUseCase(
    private val repository: JournalEntryRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(request: CreateJournalEntryRequest) {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val entry = JournalEntry(
            id = generateEntryId(),
            title = request.title.trim(),
            content = request.content.trim(),
            createdAt = now,
            updatedAt = now,
            tags = request.tags.map(String::trim).filter(String::isNotBlank),
        )
        repository.createEntry(entry)
    }
}

data class CreateJournalEntryRequest(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
)
