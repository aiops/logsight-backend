package ai.logsight.backend.users.exceptions

class UserExistsException(override val message: String? = "User already exists and is activated.") :
    RuntimeException(message)
