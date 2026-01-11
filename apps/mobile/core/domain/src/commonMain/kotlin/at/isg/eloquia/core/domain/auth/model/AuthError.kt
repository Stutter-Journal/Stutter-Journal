package at.isg.eloquia.core.domain.auth.model

import at.isg.eloquia.core.network.api.NetworkError

sealed interface AuthError {
    data object InvalidCodeOrEmail : AuthError
    data class Validation(val message: String) : AuthError
    data class Network(val error: NetworkError) : AuthError
    data class Unexpected(val message: String? = null) : AuthError
}

fun AuthError.toUserMessage(): String =
    when (this) {
        AuthError.InvalidCodeOrEmail -> "Invalid code or email"
        is AuthError.Validation -> message
        is AuthError.Network -> when (error) {
            is NetworkError.Http -> "Request failed (${error.status})"
            is NetworkError.Timeout -> "Request timed out"
            is NetworkError.Offline -> "You seem to be offline"
            is NetworkError.Cancelled -> "Request cancelled"
            is NetworkError.Decode -> "Could not read server response"
            is NetworkError.Unknown -> "Something went wrong"
        }
        is AuthError.Unexpected -> message ?: "Something went wrong"
    }
