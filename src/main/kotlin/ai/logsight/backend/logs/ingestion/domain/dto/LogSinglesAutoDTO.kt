package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessageAuto
import ai.logsight.backend.users.domain.User

data class LogSinglesAutoDTO(
    val user: User,
    val logs: List<SendLogMessageAuto>,
    val source: LogDataSources
)
