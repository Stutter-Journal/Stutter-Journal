package at.isg.eloquia.core.domain.auth.model

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}
