package ai.logsight.backend.exceptions

class EmailExistsException(override val message: String?) : LogsightApplicationException(message)
