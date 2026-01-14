package at.isg.eloquia.features.therapist.domain.exceptions

class HttpException(val status: Int, val body: String?) : Exception("HTTP $status: $body")
