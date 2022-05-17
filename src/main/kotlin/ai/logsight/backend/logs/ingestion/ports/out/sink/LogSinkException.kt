package ai.logsight.backend.logs.ingestion.ports.out.sink

class LogSinkException(override val message: String? = null) : RuntimeException(message)
