package at.isg.eloquia.core.domain.auth.repository

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest

interface AuthRepository {
    suspend fun requestLink(patientCode: String, email: String): AuthResult<LinkRequest>
}
