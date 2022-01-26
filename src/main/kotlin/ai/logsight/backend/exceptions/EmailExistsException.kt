package ai.logsight.backend.exceptions

class EmailExistsException(override val message: String? = null) : LogsightApplicationException(message)
