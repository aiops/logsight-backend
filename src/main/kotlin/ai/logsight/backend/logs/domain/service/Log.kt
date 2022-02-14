package ai.logsight.backend.logs.domain.service

data class Log(
    val app_name: String,
    val application_id: String,
    val private_key: String,
    val logFormat: String,
    val tag: String,
    val receiptId: Long,
    val message: String
)