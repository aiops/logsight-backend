package ai.logsight.backend.logs.domain

data class LogsightLog(
    val id: String? = null,
    val event: LogEvent,
    val tags: Map<String, String> = mapOf("default" to "default")
)
