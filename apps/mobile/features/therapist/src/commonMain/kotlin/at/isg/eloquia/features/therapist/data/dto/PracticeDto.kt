package at.isg.eloquia.features.therapist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PracticeDto(
    @SerialName("name") val name: String,
    @SerialName("address") val address: String? = null,
)
