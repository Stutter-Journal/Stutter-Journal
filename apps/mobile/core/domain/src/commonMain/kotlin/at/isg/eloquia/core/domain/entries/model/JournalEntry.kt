package at.isg.eloquia.core.domain.entries.model

import kotlinx.datetime.LocalDateTime

/**
 * Pure domain representation of a journal entry shared across features.
 */
data class JournalEntry(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val tags: List<String> = emptyList(),
)
