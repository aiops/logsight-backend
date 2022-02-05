package ai.logsight.backend.logs.domain

import ai.logsight.backend.application.domain.Application

data class LogsReceipt(
    val orderCounter: Long,
    val logsCount: Long,
    val source: String,
    val application: Application
)
