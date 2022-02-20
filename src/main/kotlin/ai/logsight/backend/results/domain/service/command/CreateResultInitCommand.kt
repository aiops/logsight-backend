package ai.logsight.backend.results.domain.service.command

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.users.domain.User

data class CreateResultInitCommand(
    val user: User,
    val application: Application,
    val logsReceipt: LogsReceipt
)
