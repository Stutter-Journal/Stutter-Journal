package at.isg.eloquia.core.network.ktor

import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.api.NetworkError
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NetworkClientTest {

    private val baseUrl = "http://api.eloquia.test:8080/"

    @Nested
    inner class OkCases {

        @Test
        fun `maps 200 to Ok`() = runTest {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"id":1,"name":"Ada"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val ok = result.shouldBeInstanceOf<ApiResult.Ok<UserDto>>()
            ok.status shouldBe 200
            ok.value shouldBe UserDto(id = 1, name = "Ada")
        }

        @Test
        fun `includes baseUrl and default headers`() = runTest {
            val engine = MockEngine { request ->
                request.url.host shouldBe "api.eloquia.test"
                request.url.port shouldBe 8080
                request.url.encodedPath shouldBe "/user/1"

                request.headers[HttpHeaders.Accept] shouldBe "application/json"
                request.headers[HttpHeaders.ContentType] shouldBe "application/json"

                respond(
                    content = """{"id":1,"name":"Ada"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            sut.get<UserDto>("/user/1")
        }

        @Test
        fun `adds bearer auth when token present`() = runTest {
            val tokenProvider = io.mockk.mockk<() -> String?>()
            io.mockk.every { tokenProvider.invoke() } returns "token-123"

            val engine = MockEngine { request ->
                request.headers[HttpHeaders.Authorization] shouldBe "Bearer token-123"
                respond(
                    content = """{"id":1,"name":"Ada"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val http = createHttpClient(
                baseUrl = baseUrl,
                engine = engine,
                tokenProvider = tokenProvider,
            )
            val sut = NetworkClient(http)

            sut.get<UserDto>("/user/1")

            io.mockk.verify(atLeast = 1) { tokenProvider.invoke() }
        }
    }

    @Nested
    inner class ErrorCases {

        @Test
        fun `maps non-2xx to Http error with body`() = runTest {
            val engine = MockEngine { _ ->
                respond(
                    content = "nope",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error shouldBe NetworkError.Http(status = 404, body = "nope")
        }

        @Test
        fun `maps invalid json to Decode`() = runTest {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"id":1,"name":}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error.shouldBeInstanceOf<NetworkError.Decode>()
        }

        @Test
        fun `maps timeout exception to Timeout`() = runTest {
            val engine = MockEngine { request ->
                throw HttpRequestTimeoutException(request)
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error.shouldBeInstanceOf<NetworkError.Timeout>()
        }

        @Test
        fun `maps io exception to Offline`() = runTest {
            val engine = MockEngine { _ ->
                throw kotlinx.io.IOException("offline")
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error.shouldBeInstanceOf<NetworkError.Offline>()
        }

        @Test
        fun `maps cancellation exception to Cancelled`() = runTest {
            val engine = MockEngine { _ ->
                throw CancellationException("cancelled")
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error.shouldBeInstanceOf<NetworkError.Cancelled>()
        }

        @Test
        fun `maps unknown exception to Unknown`() = runTest {
            val engine = MockEngine { _ ->
                throw IllegalStateException("boom")
            }

            val http = createHttpClient(baseUrl = baseUrl, engine = engine)
            val sut = NetworkClient(http)

            val result = sut.get<UserDto>("/user/1")

            val err = result.shouldBeInstanceOf<ApiResult.Err>()
            err.error.shouldBeInstanceOf<NetworkError.Unknown>()
        }
    }
}

@Serializable
private data class UserDto(
    val id: Int,
    val name: String,
)
