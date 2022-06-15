package ai.logsight.backend.incidents.exceptions

class IncidentNotFoundException(override val message: String? = null) : RuntimeException(message)
