package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.domain.LogsightLog

data class LogBatchDTO(
    val id: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val logs: List<LogsightLog>,
    val index: String
)
