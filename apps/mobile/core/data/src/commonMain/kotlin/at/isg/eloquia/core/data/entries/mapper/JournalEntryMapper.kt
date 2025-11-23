package at.isg.eloquia.core.data.entries.mapper

import at.isg.eloquia.core.data.entries.dto.JournalEntryDto
import at.isg.eloquia.core.domain.entries.model.JournalEntry

internal fun JournalEntryDto.toDomain(): JournalEntry = JournalEntry(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags,
)

internal fun JournalEntry.toDto(): JournalEntryDto = JournalEntryDto(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags,
)
