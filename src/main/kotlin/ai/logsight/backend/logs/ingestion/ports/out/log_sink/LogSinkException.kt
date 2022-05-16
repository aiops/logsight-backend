package ai.logsight.backend.logs.ingestion.ports.out.log_sink

class LogSinkException(override val message: String? = null) : RuntimeException(message)
