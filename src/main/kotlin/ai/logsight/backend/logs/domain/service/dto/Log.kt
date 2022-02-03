package ai.logsight.backend.logs.domain.service.dto

data class Log(
    val app_name: String,
    val private_key: String,
    val log_type: String,
    val tag: String,
    val message: String
)
