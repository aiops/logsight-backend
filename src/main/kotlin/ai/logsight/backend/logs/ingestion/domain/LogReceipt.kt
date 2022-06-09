package ai.logsight.backend.logs.ingestion.domain

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import java.util.*

data class LogReceipt(
    val id: UUID,
    val logsCount: Int,
    val processedLogCount: Int,
    val batchId: UUID,
    val status: LogBatchStatus
)
