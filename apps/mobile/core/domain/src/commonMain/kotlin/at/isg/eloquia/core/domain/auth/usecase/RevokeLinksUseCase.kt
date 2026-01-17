package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRevocation
import at.isg.eloquia.core.domain.auth.repository.AuthRepository

class RevokeLinksUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthResult<LinkRevocation> = repository.revokeLinks()
}
