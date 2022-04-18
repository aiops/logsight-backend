package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogSinglesDTO

interface LogIngestionService {
    fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt
    fun processLogSingles(logSinglesDTO: LogSinglesDTO): List<LogsReceipt>
}
