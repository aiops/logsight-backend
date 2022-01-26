package ai.logsight.backend.exceptions

class TokenNotFoundException(override val message: String?) : LogsightApplicationException(message)
