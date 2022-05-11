package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO

interface LogIngestionService {
    fun processLogBatch(logBatch: LogBatch): LogsReceipt
    fun processLogEvents(logEventsDTO: LogEventsDTO): List<LogsReceipt>
}
