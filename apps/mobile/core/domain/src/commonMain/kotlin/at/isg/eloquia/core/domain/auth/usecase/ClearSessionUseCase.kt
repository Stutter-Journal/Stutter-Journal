package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.repository.AuthSessionStore

class ClearSessionUseCase(
    private val sessionStore: AuthSessionStore,
) {
    suspend operator fun invoke() {
        sessionStore.clearSession()
    }
}
