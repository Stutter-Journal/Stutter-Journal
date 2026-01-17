package at.isg.eloquia.core.data.sync

import at.isg.eloquia.core.data.entries.datasource.JournalEntryLocalDataSource
import at.isg.eloquia.core.data.entries.dto.JournalEntryDto
import at.isg.eloquia.core.data.entries.remote.EntriesSyncApi
import at.isg.eloquia.core.data.openapi.model.ServerEntriesResponse
import at.isg.eloquia.core.data.openapi.model.ServerentryDTO
import at.isg.eloquia.core.domain.sync.DataSyncRepository
import at.isg.eloquia.core.domain.sync.SyncResult
import at.isg.eloquia.core.domain.sync.SyncSummary
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.api.NetworkError
import kotlinx.datetime.LocalDateTime

internal class DataSyncRepositoryImpl(
    private val local: JournalEntryLocalDataSource,
    private val api: EntriesSyncApi,
) : DataSyncRepository {

    override suspend fun syncNow(): SyncResult {
        val localEntries: List<JournalEntryDto> = local.getEntries()

        val pushResult = api.pushEntries(localEntries.map { it.toServerDto() })
        if (pushResult is ApiResult.Err) {
            return SyncResult.Failure(pushResult.error.toUserMessage())
        }

        return when (val pullResult: ApiResult<ServerEntriesResponse> = api.pullEntries()) {
            is ApiResult.Ok -> {
                val pulled = pullResult.value.propertyEntries.orEmpty()
                    .mapNotNull { it.toLocalDtoOrNull() }

                pulled.forEach { local.upsert(it) }

                SyncResult.Success(
                    SyncSummary(
                        pushedEntries = localEntries.size,
                        pulledEntries = pulled.size,
                    ),
                )
            }

            is ApiResult.Err -> SyncResult.Failure(pullResult.error.toUserMessage())
        }
    }
}

// TODO: Move all of these mapper functionalities into their own respective packages
private fun JournalEntryDto.toServerDto(): ServerentryDTO {
    val notes = buildString {
        if (title.isNotBlank()) {
            append(title.trim())
            append("\n\n")
        }
        append(content)
    }

    return ServerentryDTO(
        id = id,
        notes = notes,
        tags = tags,
        happenedAt = createdAt.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

private fun ServerentryDTO.toLocalDtoOrNull(): JournalEntryDto? {
    val id = id ?: return null

    val content = notes.orEmpty()
    val createdAtParsed = (createdAt ?: happenedAt ?: updatedAt)?.toLocalDateTimeOrNull() ?: return null
    val updatedAtParsed = (updatedAt ?: createdAt ?: happenedAt)?.toLocalDateTimeOrNull() ?: createdAtParsed

    return JournalEntryDto(
        id = id,
        title = situation.orEmpty(),
        content = content,
        createdAt = createdAtParsed,
        updatedAt = updatedAtParsed,
        tags = tags.orEmpty(),
    )
}

private fun String.toLocalDateTimeOrNull(): LocalDateTime? = runCatching { LocalDateTime.parse(this) }.getOrNull()

private fun NetworkError.toUserMessage(): String = when (this) {
    is NetworkError.Http -> "Sync failed (HTTP $status)"
    is NetworkError.Offline -> "You appear to be offline"
    is NetworkError.Timeout -> "Sync timed out"
    is NetworkError.Decode -> "Sync failed (bad response)"
    is NetworkError.Cancelled -> "Sync cancelled"
    is NetworkError.Unknown -> "Sync failed"
}
