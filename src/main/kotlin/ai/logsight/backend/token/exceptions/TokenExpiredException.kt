package ai.logsight.backend.token.exceptions

class TokenExpiredException(override val message: String? = "Token has expired. Please request a new token.") :
    RuntimeException(message)
