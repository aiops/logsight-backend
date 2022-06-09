package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogListDTO
import ai.logsight.backend.logs.ingestion.domain.LogReceipt

interface LogIngestionService {
    fun processLogBatch(logBatch: LogBatch): LogReceipt
    fun processLogList(logList: LogListDTO): LogReceipt

    fun processLogEvents(logEventsDTO: LogEventsDTO): LogReceipt
}
