package ai.logsight.backend.exceptions

class UserNotActivatedException(override val message: String? = null) :
    LogsightApplicationException(message)
