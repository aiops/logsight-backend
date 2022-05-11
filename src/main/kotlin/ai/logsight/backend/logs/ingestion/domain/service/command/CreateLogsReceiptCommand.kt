package ai.logsight.backend.logs.ingestion.domain.service.command

import ai.logsight.backend.application.domain.Application

data class CreateLogsReceiptCommand(
    val logsCount: Int,
    val application: Application
)
