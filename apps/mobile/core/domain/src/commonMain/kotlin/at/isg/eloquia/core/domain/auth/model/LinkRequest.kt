package at.isg.eloquia.core.domain.auth.model

data class LinkRequest(
    val linkId: String,
    val status: String?,
    val patient: Patient,
)
