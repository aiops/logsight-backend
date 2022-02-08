package ai.logsight.backend.logs.ports.web.responses

import java.util.*

data class LogsReceiptResponse(
    val appId: UUID,
    val orderId: Long,
    val logsCount: Long,
    val source: String,
)
