package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.application.domain.Application
import java.util.*

data class LogMessageDTO(
    val application: Application,
    val tag: String,
    val timestamp: String,
    val message: String,
    val level: String? = null,
    val metadata: String? = null,
)
