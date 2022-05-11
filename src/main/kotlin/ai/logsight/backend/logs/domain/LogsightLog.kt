package ai.logsight.backend.logs.domain

data class LogsightLog(
    val id: String? = null,
    val event: LogEvent,
    val metadata: Map<String, String>? = null,
    val tags: Map<String, String> = mapOf("default" to "default")
)
