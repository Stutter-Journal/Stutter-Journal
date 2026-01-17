package at.isg.eloquia.core.data.entries.remote

import at.isg.eloquia.core.data.openapi.model.ServerEntriesResponse
import at.isg.eloquia.core.data.openapi.model.ServerStatusResponse
import at.isg.eloquia.core.data.openapi.model.ServerentryDTO
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.ktor.NetworkClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Thin HTTP wrapper for entry sync calls.
 *
 * NOTE: Endpoint paths may need to be aligned with the backend implementation.
 */
internal class EntriesSyncApi(
    private val networkClient: NetworkClient,
) {
    suspend fun pullEntries(): ApiResult<ServerEntriesResponse> =
        networkClient.get(path = "/patient/entries")

    suspend fun pushEntries(entries: List<ServerentryDTO>): ApiResult<ServerStatusResponse> =
        networkClient.post(path = "/patient/entries/sync", body = EntriesSyncRequest(entries))
}

@Serializable
internal data class EntriesSyncRequest(
    @SerialName("entries") val entries: List<ServerentryDTO>,
)
