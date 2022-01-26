package ai.logsight.backend.exceptions

class TokenNotFoundException(override val message: String? = null) : LogsightApplicationException(message)
