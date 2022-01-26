package ai.logsight.backend.exceptions

open class LogsightApplicationException(override val message: String? = null) : Throwable(message = message)
