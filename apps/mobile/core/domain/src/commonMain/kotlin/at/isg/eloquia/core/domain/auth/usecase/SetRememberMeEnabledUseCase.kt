package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.repository.AuthSessionStore

class SetRememberMeEnabledUseCase(
    private val sessionStore: AuthSessionStore,
) {
    suspend operator fun invoke(enabled: Boolean) {
        sessionStore.setRememberMeEnabled(enabled)
        if (!enabled) {
            sessionStore.clearRememberedSession()
        }
    }
}
