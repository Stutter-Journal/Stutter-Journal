package at.isg.eloquia.core.data.auth.repository

import at.isg.eloquia.core.data.auth.mapper.parseServerErrorMessage
import at.isg.eloquia.core.data.auth.mapper.toDomain
import at.isg.eloquia.core.data.auth.mapper.toDomainPatient
import at.isg.eloquia.core.data.auth.remote.AuthApi
import at.isg.eloquia.core.data.openapi.model.ServerLinkInviteRequest
import at.isg.eloquia.core.data.openapi.model.ServerLinkResponse
import at.isg.eloquia.core.data.openapi.model.ServerPairingCodeRedeemRequest
import at.isg.eloquia.core.data.openapi.model.ServerPatientLoginRequest
import at.isg.eloquia.core.data.openapi.model.ServerPatientRegisterRequest
import at.isg.eloquia.core.domain.auth.model.AuthError
import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.model.Patient
import at.isg.eloquia.core.domain.auth.repository.AuthRepository
import at.isg.eloquia.core.domain.logging.AppLog
import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.api.NetworkError

internal class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    private companion object {
        const val TAG = "Auth"
    }

    override suspend fun requestLink(patientCode: String, email: String): AuthResult<LinkRequest> {
        AppLog.i(TAG, "Request link start email='${email.trim()}' codeLen=${patientCode.trim().length}")
        val result = api.requestLink(
            ServerLinkInviteRequest(
                patientCode = patientCode,
                patientEmail = email,
            ),
        )
        val mapped = result.toLinkRequestResult(
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

        when (mapped) {
            is AuthResult.Success -> AppLog.i(TAG, "Request link success linkId=${mapped.value.linkId}")
            is AuthResult.Failure -> AppLog.w(TAG, "Request link failed")
        }
        return mapped
    }

    override suspend fun redeemPairingCode(code: String): AuthResult<LinkRequest> {
        val normalized = code.trim()
        AppLog.i(TAG, "Redeem pairing code start codeLen=${normalized.length}")

        val result = api.redeemPairingCode(
            ServerPairingCodeRedeemRequest(
                code = normalized,
            ),
        )

        val mapped = result.toLinkRequestResult(
            mapHttp = { status, body ->
                when (status) {
                    400 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Invalid code")
                    401 -> AuthError.Network(NetworkError.Http(status, body))
                    404 -> AuthError.InvalidCode
                    409 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Conflict")
                    else -> AuthError.Network(NetworkError.Http(status, body))
                }
            },
        )

        when (mapped) {
            is AuthResult.Success -> AppLog.i(TAG, "Redeem pairing code success linkId=${mapped.value.linkId} status=${mapped.value.status}")
            is AuthResult.Failure -> AppLog.w(TAG, "Redeem pairing code failed")
        }

        return mapped
    }

    override suspend fun patientRegister(email: String, displayName: String, password: String): AuthResult<Patient> {
        AppLog.i(TAG, "Patient register start email='${email.trim()}' displayNameLen=${displayName.trim().length}")
        val result = api.patientRegister(
            ServerPatientRegisterRequest(
                email = email,
                displayName = displayName,
                password = password,
            ),
        )

        val mapped = result.toPatientResult(
            mapHttp = { status, body ->
                when (status) {
                    400 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Invalid registration")
                    409 -> AuthError.Validation(parseServerErrorMessage(body) ?: "An account with that email already exists")
                    else -> AuthError.Network(NetworkError.Http(status, body))
                }
            },
        )

        when (mapped) {
            is AuthResult.Success -> AppLog.i(TAG, "Patient register success patientId=${mapped.value.id}")
            is AuthResult.Failure -> AppLog.w(TAG, "Patient register failed")
        }
        return mapped
    }

    override suspend fun patientLogin(email: String, password: String): AuthResult<Patient> {
        AppLog.i(TAG, "Patient login start email='${email.trim()}'")
        val result = api.patientLogin(
            ServerPatientLoginRequest(
                email = email,
                password = password,
            ),
        )

        val mapped = result.toPatientResult(
            mapHttp = { status, body ->
                when (status) {
                    400 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Email and password are required")
                    401 -> AuthError.Validation(parseServerErrorMessage(body) ?: "Invalid email or password")
                    else -> AuthError.Network(NetworkError.Http(status, body))
                }
            },
        )

        when (mapped) {
            is AuthResult.Success -> AppLog.i(TAG, "Patient login success patientId=${mapped.value.id}")
            is AuthResult.Failure -> AppLog.w(TAG, "Patient login failed")
        }
        return mapped
    }

    override suspend fun patientMe(): AuthResult<Patient> {
        AppLog.i(TAG, "Patient me start")
        val result = api.patientMe()

        val mapped: AuthResult<Patient> = when (result) {
            is ApiResult.Ok -> {
                val patient = result.value.patient?.toDomainPatient()
                if (patient == null) {
                    AuthResult.Failure(AuthError.Unexpected("Invalid server payload"))
                } else {
                    AuthResult.Success(patient)
                }
            }

            is ApiResult.Err -> {
                val authError = when (val error = result.error) {
                    is NetworkError.Http -> when (error.status) {
                        401 -> AuthError.Validation(parseServerErrorMessage(error.body) ?: "Your session has expired")
                        else -> AuthError.Network(NetworkError.Http(error.status, error.body))
                    }

                    else -> AuthError.Network(error)
                }
                AuthResult.Failure(authError)
            }
        }

        when (mapped) {
            is AuthResult.Success -> AppLog.i(TAG, "Patient me success patientId=${mapped.value.id}")
            is AuthResult.Failure -> AppLog.w(TAG, "Patient me failed")
        }

        return mapped
    }
}

private inline fun ApiResult<at.isg.eloquia.core.data.openapi.model.ServerLinkResponse>.toLinkRequestResult(
    mapHttp: (status: Int, body: String?) -> AuthError,
): AuthResult<LinkRequest> = when (this) {
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

private inline fun ApiResult<ServerLinkResponse>.toPatientResult(
    mapHttp: (status: Int, body: String?) -> AuthError,
): AuthResult<Patient> = when (this) {
    is ApiResult.Ok -> {
        val patient = value.patient?.toDomainPatient()
        if (patient == null) {
            AuthResult.Failure(AuthError.Unexpected("Invalid server payload"))
        } else {
            AuthResult.Success(patient)
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

private fun NetworkError.toAuthError(): AuthError = when (this) {
    is NetworkError.Http -> AuthError.Network(this)
    is NetworkError.Decode -> AuthError.Network(this)
    is NetworkError.Timeout -> AuthError.Network(this)
    is NetworkError.Offline -> AuthError.Network(this)
    is NetworkError.Cancelled -> AuthError.Network(this)
    is NetworkError.Unknown -> AuthError.Network(this)
}
