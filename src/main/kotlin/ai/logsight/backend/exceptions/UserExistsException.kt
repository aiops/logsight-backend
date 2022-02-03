package ai.logsight.backend.exceptions

class UserExistsException(override val message: String? = null) :
    LogsightApplicationException(message)
