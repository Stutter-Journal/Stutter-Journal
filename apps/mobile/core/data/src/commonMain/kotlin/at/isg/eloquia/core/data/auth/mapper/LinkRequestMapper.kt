package at.isg.eloquia.core.data.auth.mapper

import at.isg.eloquia.core.data.openapi.model.ServerLinkResponse
import at.isg.eloquia.core.data.openapi.model.ServerlinkDTO
import at.isg.eloquia.core.domain.auth.model.LinkRequest

internal fun ServerLinkResponse.toDomain(): LinkRequest? {
    val patient = patient?.toDomainPatient() ?: return null
    val link = link ?: return null
    val linkId = link.id ?: return null

    return LinkRequest(
        linkId = linkId,
        status = link.status,
        patient = patient,
    )
}

@Suppress("unused")
private fun ServerlinkDTO.toDomainIdOrNull(): String? = id
