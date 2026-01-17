package at.isg.eloquia.core.network.api

sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T, val status: Int) : ApiResult<T>
    data class Err(val error: NetworkError) : ApiResult<Nothing>
}

sealed interface NetworkError {
    data class Http(val status: Int, val body: String?) : NetworkError
    data class Decode(val message: String, val cause: Throwable? = null) : NetworkError
    data class Timeout(val cause: Throwable? = null) : NetworkError
    data class Offline(val cause: Throwable? = null) : NetworkError
    data class Cancelled(val cause: Throwable? = null) : NetworkError
    data class Unknown(val cause: Throwable? = null) : NetworkError
}
