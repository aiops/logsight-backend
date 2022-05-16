package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

interface LogSinkAdapter {
    fun sendBatch(logBatchDTO: LogBatchDTO)
}
