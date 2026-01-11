package at.isg.eloquia.core.data.auth.repository

import at.isg.eloquia.core.data.auth.mapper.toDomain
import at.isg.eloquia.core.data.auth.mapper.parseServerErrorMessage
import at.isg.eloquia.core.data.auth.remote.AuthApi
import at.isg.eloquia.core.data.openapi.model.ServerLinkInviteRequest
import at.isg.eloquia.core.domain.auth.model.AuthError
import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.repository.AuthRepository
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.api.NetworkError

internal class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun requestLink(patientCode: String, email: String): AuthResult<LinkRequest> {
        val result = api.requestLink(
            ServerLinkInviteRequest(
                patientCode = patientCode,
                patientEmail = email,
            ),
        )
        return result.toLinkRequestResult(
            mapHttp = { status, body ->
                when (status) {
                    400 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Invalid request")
                    401 -> AuthError.Network(NetworkError.Http(status, body))
                    409 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Conflict")
                    404 -> AuthError.InvalidCodeOrEmail
                    else -> AuthError.Network(NetworkError.Http(status, body))
                }
            },
        )
    }
}

private inline fun ApiResult<at.isg.eloquia.core.data.openapi.model.ServerLinkResponse>.toLinkRequestResult(
    mapHttp: (status: Int, body: String?) -> AuthError,
): AuthResult<LinkRequest> =
    when (this) {
        is ApiResult.Ok -> {
            val linkRequest = value.toDomain()
            if (linkRequest == null) {
                AuthResult.Failure(AuthError.Unexpected("Invalid server payload"))
            } else {
                AuthResult.Success(linkRequest)
            }
        }

        is ApiResult.Err -> {
            val authError = when (val error = error) {
                is NetworkError.Http -> mapHttp(error.status, error.body)
                else -> AuthError.Network(error)
            }
            AuthResult.Failure(authError)
        }
    }

private fun NetworkError.toAuthError(): AuthError =
    when (this) {
        is NetworkError.Http -> AuthError.Network(this)
        is NetworkError.Decode -> AuthError.Network(this)
        is NetworkError.Timeout -> AuthError.Network(this)
        is NetworkError.Offline -> AuthError.Network(this)
        is NetworkError.Cancelled -> AuthError.Network(this)
        is NetworkError.Unknown -> AuthError.Network(this)
    }
