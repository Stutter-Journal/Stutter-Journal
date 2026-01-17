package at.isg.eloquia.features.therapist.data.mapper

import at.isg.eloquia.features.therapist.data.dto.DoctorDto
import at.isg.eloquia.features.therapist.data.dto.PracticeDto
import at.isg.eloquia.features.therapist.domain.model.Practice
import at.isg.eloquia.features.therapist.domain.model.Therapist

fun DoctorDto.toDomain(): Therapist = Therapist(
    email = email,
    displayName = displayName,
    practice = practice.toDomain(),
)

fun PracticeDto.toDomain(): Practice = Practice(
    name = name,
    address = address,
)
