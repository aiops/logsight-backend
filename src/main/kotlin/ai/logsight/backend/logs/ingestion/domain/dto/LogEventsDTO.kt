package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.domain.User

data class LogEventsDTO(
    val user: User,
    val logs: List<SendLogMessage>
)
