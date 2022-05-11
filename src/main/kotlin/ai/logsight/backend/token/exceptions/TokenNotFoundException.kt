package ai.logsight.backend.token.exceptions

class TokenNotFoundException(override val message: String? = null) : RuntimeException(message)
