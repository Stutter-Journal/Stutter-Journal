package at.isg.eloquia.features.entries.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model representing a journal entry
 */
data class JournalEntry(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: List<String> = emptyList()
)
