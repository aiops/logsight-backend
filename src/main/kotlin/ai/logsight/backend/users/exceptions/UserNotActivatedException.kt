package ai.logsight.backend.users.exceptions

class UserNotActivatedException(override val message: String? = null) :
    RuntimeException(message)
