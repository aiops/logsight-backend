package ai.logsight.backend.application.exceptions

class ApplicationNotFoundException(override val message: String? = null) : RuntimeException(message)
