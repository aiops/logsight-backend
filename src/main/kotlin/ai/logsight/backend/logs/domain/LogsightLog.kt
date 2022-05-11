package ai.logsight.backend.logs.domain

import ai.logsight.backend.logs.domain.enums.LogDataSources

data class LogsightLog(
    val app_name: String,
    val application_id: String,
    val private_key: String,
    val source: LogDataSources,
    val tag: String,
    val orderCounter: Long,
    val message: LogMessage
)
