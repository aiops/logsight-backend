package ai.logsight.backend.exceptions

class UserNotFoundException(override val message: String? = null) : LogsightApplicationException(message)
