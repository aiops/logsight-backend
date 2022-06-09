package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.domain.LogsightLog

class LogListDTO(
    val index: String,
    val logs: List<LogsightLog>,
    val tags: Map<String, String> = mapOf("default" to "default")
)
