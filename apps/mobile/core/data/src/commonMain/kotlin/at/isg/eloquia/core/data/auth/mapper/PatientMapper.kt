package at.isg.eloquia.core.data.auth.mapper

import at.isg.eloquia.core.data.openapi.model.ServerPatientResponse
import at.isg.eloquia.core.data.openapi.model.ServerpatientDTO
import at.isg.eloquia.core.domain.auth.model.Patient

internal fun ServerpatientDTO.toDomainPatient(): Patient? {
    val id = id ?: return null
    val email = email ?: return null

    return Patient(
        id = id,
        email = email,
        displayName = displayName,
        patientCode = patientCode,
    )
}

internal fun ServerPatientResponse.toDomainPatient(): Patient? {
    val id = id ?: return null
    val email = email ?: return null

    return Patient(
        id = id,
        email = email,
        displayName = displayName,
        patientCode = null,
    )
}
