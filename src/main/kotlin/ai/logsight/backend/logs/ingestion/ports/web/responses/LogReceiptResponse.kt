package ai.logsight.backend.logs.ingestion.ports.web.responses

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import java.util.*

data class LogReceiptResponse(
    val receiptId: UUID,
    val logsCount: Int,
    val batchId: UUID,
    val status: LogBatchStatus
)
