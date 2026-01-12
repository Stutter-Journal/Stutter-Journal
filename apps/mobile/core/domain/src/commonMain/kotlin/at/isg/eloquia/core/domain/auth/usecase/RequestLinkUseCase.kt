package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.repository.AuthRepository

class RequestLinkUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(patientCode: String, email: String): AuthResult<LinkRequest> = repository.requestLink(patientCode = patientCode, email = email)
}
