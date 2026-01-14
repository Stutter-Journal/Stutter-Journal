package at.isg.eloquia.features.therapist.domain.exceptions

class TimeoutException(cause: Throwable?) : Exception("Request timed out", cause)
