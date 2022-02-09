package ai.logsight.backend.token.exceptions

class TokenExpiredException(override val message: String? = null) : RuntimeException(message)
