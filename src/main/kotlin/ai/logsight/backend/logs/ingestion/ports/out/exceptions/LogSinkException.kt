package ai.logsight.backend.logs.ingestion.ports.out.exceptions

class LogSinkException(override val message: String? = null) : RuntimeException(message)
