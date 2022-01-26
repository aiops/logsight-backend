package ai.logsight.backend.exceptions

class TokenExpiredException(override val message: String?) : LogsightApplicationException(message)
