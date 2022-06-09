package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.domain.LogEvent

class LogListDTO(
    val index: String,
    val logs: List<LogEvent>,
    val tags: Map<String, String> = mapOf("default" to "default")
)
