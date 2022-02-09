package ai.logsight.backend.users.exceptions

class UserExistsException(override val message: String? = null) :
    RuntimeException(message)
