package at.isg.eloquia.features.therapist.domain.exceptions

class OfflineException(cause: Throwable?) : Exception("No internet connection", cause)
