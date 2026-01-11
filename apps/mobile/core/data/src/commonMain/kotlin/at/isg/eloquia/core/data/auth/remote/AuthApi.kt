package at.isg.eloquia.core.data.auth.remote

import at.isg.eloquia.core.data.openapi.model.ServerLinkInviteRequest
import at.isg.eloquia.core.data.openapi.model.ServerLinkResponse
import at.isg.eloquia.core.data.openapi.model.ServerpatientDTO
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.ktor.NetworkClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class AuthApi(
    private val networkClient: NetworkClient,
) {
    suspend fun requestLink(request: ServerLinkInviteRequest): ApiResult<ServerLinkResponse> =
        networkClient.post(path = "/links/request", body = request)

    suspend fun patientRegister(request: PatientRegisterRequest): ApiResult<PatientAuthResponse> =
        networkClient.post(path = "/patient/register", body = request)

    suspend fun patientLogin(request: PatientLoginRequest): ApiResult<PatientAuthResponse> =
        networkClient.post(path = "/patient/login", body = request)
}

@Serializable
internal data class PatientRegisterRequest(
    @SerialName("email") val email: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("password") val password: String,
)

@Serializable
internal data class PatientLoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)

@Serializable
internal data class PatientAuthResponse(
    @SerialName("patient") val patient: ServerpatientDTO? = null,
)
