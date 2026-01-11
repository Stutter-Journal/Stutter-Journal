package at.isg.eloquia.core.data.auth.mapper

import at.isg.eloquia.core.data.openapi.model.ServerLinkResponse
import at.isg.eloquia.core.data.openapi.model.ServerlinkDTO
import at.isg.eloquia.core.data.openapi.model.ServerpatientDTO
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.model.Patient

internal fun ServerLinkResponse.toDomain(): LinkRequest? {
    val patient = patient?.toDomain() ?: return null
    val link = link ?: return null
    val linkId = link.id ?: return null

    return LinkRequest(
        linkId = linkId,
        status = link.status,
        patient = patient,
    )
}

private fun ServerpatientDTO.toDomain(): Patient? {
    val id = id ?: return null
    val email = email ?: return null

    return Patient(
        id = id,
        email = email,
        displayName = displayName,
        patientCode = patientCode,
    )
}

@Suppress("unused")
private fun ServerlinkDTO.toDomainIdOrNull(): String? = id
