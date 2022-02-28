package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.users.domain.User

data class LogBatchDTO(
    val user: User,
    val application: Application,
    val tag: String,
    val logs: List<LogMessage>,
    val source: LogDataSources
)
