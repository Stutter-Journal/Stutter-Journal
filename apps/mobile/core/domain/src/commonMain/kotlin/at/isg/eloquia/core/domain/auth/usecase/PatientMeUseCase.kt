package at.isg.eloquia.core.domain.auth.usecase

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.Patient
import at.isg.eloquia.core.domain.auth.repository.AuthRepository

class PatientMeUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthResult<Patient> = repository.patientMe()
}
