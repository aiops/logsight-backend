package ai.logsight.backend.users.exceptions

class UserExistsException(override val message: String? = "User is already activated.") :
    RuntimeException(message)
