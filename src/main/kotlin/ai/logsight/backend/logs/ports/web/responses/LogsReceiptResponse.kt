package ai.logsight.backend.logs.ports.web.responses

import java.util.*

data class LogsReceiptResponse(
    val id: UUID,
    val orderNum: Long,
    val logsCount: Int,
    val source: String,
    val appId: UUID
)
