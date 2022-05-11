package ai.logsight.backend.logs.ingestion.ports.out.sink

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

interface Sink {
    fun sendBatch(logBatchDTO: LogBatchDTO)
    fun sendString(content: String)
}
