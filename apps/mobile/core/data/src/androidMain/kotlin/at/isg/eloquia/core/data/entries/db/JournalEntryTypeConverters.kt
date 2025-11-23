package at.isg.eloquia.core.data.entries.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal object JournalEntryTypeConverters {
    @TypeConverter
    fun fromTags(value: List<String>): String = value.joinToString(separator = "|")

    @TypeConverter
    fun toTags(value: String): List<String> = if (value.isBlank()) emptyList() else value.split("|")

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): Long = value.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    @TypeConverter
    fun toLocalDateTime(value: Long): LocalDateTime = Instant.fromEpochMilliseconds(value).toLocalDateTime(TimeZone.currentSystemDefault())
}
