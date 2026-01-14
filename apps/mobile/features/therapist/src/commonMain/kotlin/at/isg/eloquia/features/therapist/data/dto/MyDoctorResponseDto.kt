package at.isg.eloquia.features.therapist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyDoctorResponseDto(
    @SerialName("doctor") val doctor: DoctorDto,
)
