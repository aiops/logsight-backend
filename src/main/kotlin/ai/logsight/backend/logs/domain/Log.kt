package ai.logsight.backend.logs.domain

data class Log(
    val app_name: String,
    val application_id: String,
    val private_key: String,
    val logFormat: String,
    val tag: String,
    val orderCounter: Long,
    val message: String
)
