package at.isg.eloquia.core.domain.sync

/**
 * Cross-platform abstraction for synchronising local data with the backend.
 *
 * Current implementation focuses on journal entries, but this is intentionally generic so it can
 * later include other patient-owned data.
 */
interface DataSyncRepository {
    suspend fun syncNow(): SyncResult
}

data class SyncSummary(
    val pushedEntries: Int,
    val pulledEntries: Int,
)

sealed interface SyncResult {
    data class Success(val summary: SyncSummary) : SyncResult
    data class Failure(val message: String) : SyncResult
}
