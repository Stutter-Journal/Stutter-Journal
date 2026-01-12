package at.isg.eloquia.core.domain.auth.repository

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.LinkRequest
import at.isg.eloquia.core.domain.auth.model.Patient

interface AuthRepository {
    suspend fun requestLink(patientCode: String, email: String): AuthResult<LinkRequest>

    suspend fun patientRegister(email: String, displayName: String, password: String): AuthResult<Patient>

    suspend fun patientLogin(email: String, password: String): AuthResult<Patient>

    /** Validates the current session (cookie-based) and returns the logged-in patient. */
    suspend fun patientMe(): AuthResult<Patient>
}
