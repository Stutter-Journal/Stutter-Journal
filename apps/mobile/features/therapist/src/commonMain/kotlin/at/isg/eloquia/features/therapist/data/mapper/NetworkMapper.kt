package at.isg.eloquia.features.therapist.data.mapper

import at.isg.eloquia.core.network.api.NetworkError
import at.isg.eloquia.features.therapist.domain.exceptions.CancelledException
import at.isg.eloquia.features.therapist.domain.exceptions.DecodeException
import at.isg.eloquia.features.therapist.domain.exceptions.HttpException
import at.isg.eloquia.features.therapist.domain.exceptions.OfflineException
import at.isg.eloquia.features.therapist.domain.exceptions.TherapistNotFoundException
import at.isg.eloquia.features.therapist.domain.exceptions.TimeoutException
import at.isg.eloquia.features.therapist.domain.exceptions.UnauthorizedException
import at.isg.eloquia.features.therapist.domain.exceptions.UnknownException

internal fun NetworkError.toException(): Exception = when (this) {
    is NetworkError.Http -> {
        when (status) {
            404 -> TherapistNotFoundException("No therapist assigned")
            401 -> UnauthorizedException("Unauthorized")
            else -> HttpException(status, body)
        }
    }

    is NetworkError.Offline -> OfflineException(cause)
    is NetworkError.Timeout -> TimeoutException(cause)
    is NetworkError.Decode -> DecodeException(message, cause)
    is NetworkError.Cancelled -> CancelledException(cause)
    is NetworkError.Unknown -> UnknownException(cause)
}
