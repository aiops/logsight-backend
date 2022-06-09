package ai.logsight.backend.logs.ingestion.exceptions

class LogReceiptNotFoundException(override val message: String? = null) : RuntimeException(message)
