package ai.logsight.backend.logs.ingestion.domain.service.command

import java.util.*

data class CreateLogsReceiptCommand(
    val batchId: UUID,
    val logsCount: Int
)
