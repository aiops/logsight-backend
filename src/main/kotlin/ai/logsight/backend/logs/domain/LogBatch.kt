package ai.logsight.backend.logs.domain

import java.util.*

data class LogBatch(
    val id: UUID = UUID.randomUUID(),
    val logs: List<LogsightLog>,
    val index: String,
    val metadata: Map<String, Any> = emptyMap()
)
