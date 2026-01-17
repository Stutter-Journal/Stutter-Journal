package at.isg.eloquia.features.therapist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoctorDto(
    @SerialName("email") val email: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("myDoctorPractice") val practice: PracticeDto,
)
