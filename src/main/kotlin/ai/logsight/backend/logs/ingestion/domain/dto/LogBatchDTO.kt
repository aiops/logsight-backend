package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.domain.LogsightLog
import java.util.*

data class LogBatchDTO(
    val id: UUID,
    val logs: List<LogsightLog>,
    val index: String
)
