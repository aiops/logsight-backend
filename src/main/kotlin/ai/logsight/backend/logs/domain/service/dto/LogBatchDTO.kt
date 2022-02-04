package ai.logsight.backend.logs.domain.service.dto

import ai.logsight.backend.logs.domain.LogFormat
import java.util.*

data class LogBatchDTO(
    val userEmail: String,
    val applicationId: UUID,
    val tag: String,
    val logFormat: LogFormat,
    val logs: List<String>
)
