package ai.logsight.backend.logs.ingestion.exceptions

class LogQueueCapacityLimitReached(override val message: String? = null) : RuntimeException(message)
