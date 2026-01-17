package at.isg.eloquia.core.data.auth.dto

import at.isg.eloquia.core.data.openapi.model.ServerpatientDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientMeResponseDto(
    @SerialName("patient") val patient: ServerpatientDTO? = null,
)
