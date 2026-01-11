package at.isg.eloquia.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.URLBuilder
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
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
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
    engine: HttpClientEngine = defaultHttpClientEngine(),
): HttpClient = buildHttpClient(
    engine = engine,
    baseUrl = URLBuilder().takeFrom(baseUrl).build(),
    tokenProvider = tokenProvider,
    enableLogging = enableLogging,
)
