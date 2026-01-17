package at.isg.eloquia.core.network.ktor

import io.github.aakira.napier.Napier
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.logging.HttpLoggingInterceptor

internal actual fun defaultHttpClientEngine(enableLogging: Boolean): HttpClientEngine = OkHttp.create {
    if (enableLogging) {
        config {
            val interceptor = HttpLoggingInterceptor { message ->
                Napier.i(message = message, tag = "OKHTTP")
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
                redactHeader("Cookie")
            }

            addInterceptor(interceptor)
        }
    }
}
