package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.Patient
import at.isg.eloquia.core.domain.auth.repository.AuthRepository

class PatientRegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, displayName: String, password: String): AuthResult<Patient> = repository.patientRegister(email = email, displayName = displayName, password = password)
}
