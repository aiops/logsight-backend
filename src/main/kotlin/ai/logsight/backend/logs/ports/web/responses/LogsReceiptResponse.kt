package ai.logsight.backend.logs.ports.web.responses

import java.util.*

data class LogsReceiptResponse(
    val receiptId: UUID,
    val logsCount: Int,
    val source: String,
    val applicationId: UUID
)
