package at.isg.eloquia.core.data.auth.mapper

import at.isg.eloquia.core.data.openapi.model.ServerErrorResponse
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

internal fun parseServerErrorMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return runCatching { json.decodeFromString<ServerErrorResponse>(body).error }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
}
