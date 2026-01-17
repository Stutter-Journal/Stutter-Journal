package at.isg.eloquia.features.therapist.domain.repository

import at.isg.eloquia.features.therapist.domain.model.Therapist
import kotlinx.coroutines.flow.Flow

interface TherapistRepository {
    fun getMyTherapist(): Flow<Result<Therapist>>
}
