package at.isg.eloquia.core.data.entries.db

import at.isg.eloquia.core.data.entries.dto.JournalEntryDto

internal fun JournalEntryEntity.toDto(): JournalEntryDto = JournalEntryDto(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags,
)

internal fun JournalEntryDto.toEntity(): JournalEntryEntity = JournalEntryEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags,
)
