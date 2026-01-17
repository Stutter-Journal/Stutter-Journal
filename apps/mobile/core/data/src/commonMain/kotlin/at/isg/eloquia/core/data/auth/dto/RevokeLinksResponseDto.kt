package at.isg.eloquia.core.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevokeLinksResponseDto(
    @SerialName("revoked") val revoked: Int = 0,
)
