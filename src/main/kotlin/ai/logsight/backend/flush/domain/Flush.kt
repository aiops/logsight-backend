package ai.logsight.backend.flush.domain

import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.flush.domain.service.FlushStatus
import java.util.*

data class Flush(
    val id: UUID,
    val status: FlushStatus,
    val logsReceipt: LogsReceipt
)
