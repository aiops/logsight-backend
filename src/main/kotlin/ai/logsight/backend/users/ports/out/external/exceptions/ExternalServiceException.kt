package ai.logsight.backend.users.ports.out.external.exceptions

class ExternalServiceException(override val message: String? = null) : RuntimeException(message)
