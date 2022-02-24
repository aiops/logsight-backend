package ai.logsight.backend.logs.domain

import ai.logsight.backend.logs.ingestion.ports.web.requests.LogRequest

data class Log(
    val app_name: String,
    val application_id: String,
    val private_key: String,
    val tag: String,
    val orderCounter: Long,
    val message: LogRequest
)
