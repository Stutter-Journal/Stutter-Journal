package at.isg.eloquia.features.therapist.data.api

import at.isg.eloquia.core.network.api.ApiResult
import at.isg.eloquia.core.network.ktor.NetworkClient
import at.isg.eloquia.features.therapist.data.dto.MyDoctorResponseDto

interface TherapistApi {
    suspend fun getMyDoctor(): ApiResult<MyDoctorResponseDto>
}

class TherapistApiImpl(
    private val client: NetworkClient,
) : TherapistApi {

    override suspend fun getMyDoctor(): ApiResult<MyDoctorResponseDto> = client.get(path = "/patient/mydoctor")
}
