package logCount

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import java.util.*

data class LogReceipt(
    val id: UUID,
    val logCount: Int,
    val processedLogCount: Int,
    val batchId: UUID,
    val status: LogBatchStatus
)
