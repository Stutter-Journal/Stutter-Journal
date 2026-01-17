package at.isg.eloquia.core.domain.sync

class SyncNowUseCase(
    private val repository: DataSyncRepository,
) {
    suspend operator fun invoke(): SyncResult = repository.syncNow()
}
