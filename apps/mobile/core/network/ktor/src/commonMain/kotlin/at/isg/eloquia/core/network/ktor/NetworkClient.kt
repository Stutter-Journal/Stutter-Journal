package at.isg.eloquia.core.network.ktor

import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.api.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

class NetworkClient(@PublishedApi internal val client: HttpClient) {

    suspend inline fun <reified T : Any> get(
        path: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResult<T> = request(HttpMethod.Get, path, headers, queryParams)

    suspend inline fun <reified T : Any, reified B : Any> post(
        path: String,
        body: B,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResult<T> = request(HttpMethod.Post, path, headers, queryParams) {
        setBody(body)
    }

    suspend inline fun <reified T : Any> post(
        path: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResult<T> = request(HttpMethod.Post, path, headers, queryParams)

    suspend inline fun <reified T : Any> request(
        method: HttpMethod,
        path: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): ApiResult<T> = try {
        val response: HttpResponse = client.request(path) {
            this.method = method
            headers.forEach { (key, value) -> header(key, value) }
            queryParams.forEach { (key, value) -> parameter(key, value) }
            block()
        }

        val status = response.status.value
        if (status in 200..299) {
            val value: T = if ((status == 204 || status == 205) && T::class == Unit::class) {
                @Suppress("UNCHECKED_CAST")
                Unit as T
            } else {
                response.body()
            }
            ApiResult.Ok(value, status)
        } else {
            val bodyText = runCatching { response.bodyAsText() }.getOrNull()
            ApiResult.Err(NetworkError.Http(status, bodyText))
        }
    } catch (ce: CancellationException) {
        ApiResult.Err(NetworkError.Cancelled(ce))
    } catch (t: Throwable) {
        ApiResult.Err(t.toNetworkError())
    }
}

@PublishedApi
internal fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpRequestTimeoutException -> NetworkError.Timeout(this)
    is SocketTimeoutException -> NetworkError.Timeout(this)
    is IOException -> NetworkError.Offline(this)
    is SerializationException -> NetworkError.Decode(message ?: "Serialization error", this)
    is JsonConvertException -> NetworkError.Decode(message ?: "Conversion error", this)
    is ContentConvertException -> NetworkError.Decode(message ?: "Conversion error", this)
    else -> NetworkError.Unknown(this)
}
