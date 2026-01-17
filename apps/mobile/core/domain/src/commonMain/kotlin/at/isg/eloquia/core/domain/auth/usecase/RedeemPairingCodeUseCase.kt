package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.repository.AuthRepository

class RedeemPairingCodeUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(code: String): AuthResult<LinkRequest> = repository.redeemPairingCode(code = code)
}
