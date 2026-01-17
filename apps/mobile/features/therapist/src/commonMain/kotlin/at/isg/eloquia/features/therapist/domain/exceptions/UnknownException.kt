package at.isg.eloquia.features.therapist.domain.exceptions

class UnknownException(cause: Throwable?) : Exception("Unknown error", cause)
