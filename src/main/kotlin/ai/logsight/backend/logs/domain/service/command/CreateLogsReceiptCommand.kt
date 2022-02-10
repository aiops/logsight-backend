package ai.logsight.backend.logs.domain.service.command

import ai.logsight.backend.application.domain.Application

data class CreateLogsReceiptCommand(
    val logsCount: Int,
    val source: String,
    val application: Application
)
