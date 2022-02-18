package ai.logsight.backend.results.domain

import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.results.domain.service.ResultInitStatus
import java.util.*

data class ResultInit(
    val id: UUID,
    val status: ResultInitStatus,
    val logsReceipt: LogsReceipt
)
