package at.isg.eloquia.features.therapist.domain.model

data class Therapist(
    val email: String,
    val displayName: String,
    val practice: Practice,
)
