package ai.logsight.backend.logs.domain.service

data class Log(
    val appName: String,
    val privateKey: String,
    val logFormat: String,
    val tag: String,
    val receiptId: Long,
    val message: String
)
