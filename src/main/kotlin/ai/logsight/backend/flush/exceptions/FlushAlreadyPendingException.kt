package ai.logsight.backend.flush.exceptions

class FlushAlreadyPendingException(override val message: String? = null) : RuntimeException(message)
