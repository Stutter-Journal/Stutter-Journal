package at.isg.eloquia.core.domain.auth.model

data class Patient(
    val id: String,
    val email: String,
    val displayName: String?,
    val patientCode: String?,
)
