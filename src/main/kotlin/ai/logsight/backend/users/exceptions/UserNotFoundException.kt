package ai.logsight.backend.users.exceptions

class UserNotFoundException(override val message: String? = null) : RuntimeException(message)
