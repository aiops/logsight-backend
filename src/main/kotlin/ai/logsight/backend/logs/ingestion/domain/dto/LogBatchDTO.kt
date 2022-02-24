package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.domain.enums.LogFormats
import ai.logsight.backend.logs.ingestion.ports.web.requests.LogRequest
import ai.logsight.backend.users.domain.User

data class LogBatchDTO(
    val user: User,
    val application: Application,
    val tag: String,
    val logs: List<LogRequest>,
    val source: LogDataSources
)
