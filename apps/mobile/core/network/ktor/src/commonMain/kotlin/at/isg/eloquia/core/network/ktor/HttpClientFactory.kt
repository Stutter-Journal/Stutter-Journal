package at.isg.eloquia.core.network.ktor

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun buildHttpClient(
    engine: HttpClientEngine,
    baseUrl: Url,
    tokenProvider: () -> String?,
    enableLogging: Boolean,
): HttpClient = HttpClient(engine) {
    expectSuccess = false

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            val extra = if (cause is ResponseException) {
                val body = runCatching { cause.response.bodyAsText() }.getOrNull()
                " status=${cause.response.status.value} body=$body"
            } else {
                ""
            }
            Napier.e(
                message = "HTTP exception: ${request.method.value} ${request.url}$extra",
                throwable = cause,
                tag = "HTTP",
            )
        }
    }
    defaultRequest {
        url.takeFrom(baseUrl)
        header(HttpHeaders.Accept, ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
            },
        )
    }

    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 30_000
    }

    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 2)
        exponentialDelay()
    }

    if (enableLogging) {
        Napier.i(message = "Installing Ktor Logging plugin (level=ALL)", tag = "HTTP")
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    // Use INFO so logs show up in typical Logcat filters.
                    Napier.i(message = message, tag = "HTTP")
                }
            }
            sanitizeHeader { header ->
                header.equals(HttpHeaders.Authorization, ignoreCase = true)
            }
            level = LogLevel.ALL
        }
    }

    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenProvider() ?: return@loadTokens null
                BearerTokens(token, "")
            }
        }
    }
}

fun createHttpClient(
    baseUrl: String,
    tokenProvider: () -> String? = { null },
    enableLogging: Boolean = false,
    engine: HttpClientEngine = defaultHttpClientEngine(enableLogging),
): HttpClient = buildHttpClient(
    engine = engine,
    baseUrl = URLBuilder().takeFrom(baseUrl).build(),
    tokenProvider = tokenProvider,
    enableLogging = enableLogging,
)
