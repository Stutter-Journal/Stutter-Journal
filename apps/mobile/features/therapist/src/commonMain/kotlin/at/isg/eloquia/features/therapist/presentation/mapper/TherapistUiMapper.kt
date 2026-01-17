package at.isg.eloquia.features.therapist.presentation.mapper

import at.isg.eloquia.features.therapist.domain.model.Therapist
import at.isg.eloquia.features.therapist.presentation.model.TherapistUi

fun Therapist.toUi(): TherapistUi = TherapistUi(
    email = email,
    displayName = displayName,
    practiceName = practice.name,
    practiceAddress = practice.address ?: "No address provided",
)
