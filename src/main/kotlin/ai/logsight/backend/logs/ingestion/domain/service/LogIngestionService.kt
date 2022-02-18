package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

interface LogIngestionService {
    fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt
}
