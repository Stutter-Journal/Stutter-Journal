package at.isg.eloquia.features.therapist.domain.exceptions

class CancelledException(cause: Throwable?) : Exception("Request cancelled", cause)
