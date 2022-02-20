package ai.logsight.backend.users.exceptions

class UserAlreadyActivatedException(override val message: String? = "User is already activated. Please login.") :
    RuntimeException(message)
