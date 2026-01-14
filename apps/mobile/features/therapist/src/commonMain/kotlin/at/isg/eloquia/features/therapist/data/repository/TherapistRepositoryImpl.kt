package at.isg.eloquia.features.therapist.data.repository

import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.features.therapist.data.api.TherapistApi
import at.isg.eloquia.features.therapist.data.mapper.toDomain
import at.isg.eloquia.features.therapist.data.mapper.toException
import at.isg.eloquia.features.therapist.domain.model.Therapist
import at.isg.eloquia.features.therapist.domain.repository.TherapistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TherapistRepositoryImpl(
    private val api: TherapistApi,
) : TherapistRepository {

    override fun getMyTherapist(): Flow<Result<Therapist>> = flow {
        when (val result = api.getMyDoctor()) {
            is ApiResult.Ok -> {
                val therapist = result.value.doctor.toDomain()
                emit(Result.success(therapist))
            }

            is ApiResult.Err -> {
                emit(Result.failure(result.error.toException()))
            }
        }
    }
}
