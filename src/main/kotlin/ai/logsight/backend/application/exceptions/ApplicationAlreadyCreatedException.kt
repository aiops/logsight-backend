package ai.logsight.backend.application.exceptions

class ApplicationAlreadyCreatedException(override val message: String? = null) : RuntimeException(message)
