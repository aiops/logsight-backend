package ai.logsight.backend.users.exceptions

class EmailExistsException(override val message: String? = null) : RuntimeException(message)
