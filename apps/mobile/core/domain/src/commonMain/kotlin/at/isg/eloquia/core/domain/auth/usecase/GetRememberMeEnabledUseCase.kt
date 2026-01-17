package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.repository.AuthSessionStore

class GetRememberMeEnabledUseCase(
    private val sessionStore: AuthSessionStore,
) {
    suspend operator fun invoke(): Boolean = sessionStore.isRememberMeEnabled()
}
