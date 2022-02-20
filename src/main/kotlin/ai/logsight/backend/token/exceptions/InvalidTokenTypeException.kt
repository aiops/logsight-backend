package ai.logsight.backend.token.exceptions

class InvalidTokenTypeException(override val message: String? = "Token is invalid. Please request a new token.") :
    RuntimeException(message)
