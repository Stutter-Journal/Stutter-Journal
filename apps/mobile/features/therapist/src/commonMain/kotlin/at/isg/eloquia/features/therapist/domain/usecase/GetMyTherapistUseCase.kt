package at.isg.eloquia.features.therapist.domain.usecase

import at.isg.eloquia.features.therapist.domain.model.Therapist
import at.isg.eloquia.features.therapist.domain.repository.TherapistRepository
import kotlinx.coroutines.flow.Flow

class GetMyTherapistUseCase(
    private val repository: TherapistRepository,
) {
    operator fun invoke(): Flow<Result<Therapist>> = repository.getMyTherapist()
}
