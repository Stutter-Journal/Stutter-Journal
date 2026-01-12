package at.isg.eloquia.core.data.auth.remote

import at.isg.eloquia.core.data.auth.dto.PatientMeResponseDto
import at.isg.eloquia.core.data.openapi.model.ServerLinkInviteRequest
import at.isg.eloquia.core.data.openapi.model.ServerLinkResponse
import at.isg.eloquia.core.data.openapi.model.ServerPatientLoginRequest
import at.isg.eloquia.core.data.openapi.model.ServerPatientRegisterRequest
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.ktor.NetworkClient

internal class AuthApi(
    private val networkClient: NetworkClient,
) {
    suspend fun requestLink(request: ServerLinkInviteRequest): ApiResult<ServerLinkResponse> = networkClient.post(path = "/links/request", body = request)

    suspend fun patientRegister(request: ServerPatientRegisterRequest): ApiResult<ServerLinkResponse> = networkClient.post(path = "/patient/register", body = request)

    suspend fun patientLogin(request: ServerPatientLoginRequest): ApiResult<ServerLinkResponse> = networkClient.post(path = "/patient/login", body = request)

    suspend fun patientMe(): ApiResult<PatientMeResponseDto> = networkClient.get(path = "/patient/me")
}
