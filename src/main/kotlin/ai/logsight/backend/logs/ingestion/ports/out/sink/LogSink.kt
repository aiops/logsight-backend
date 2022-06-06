package ai.logsight.backend.logs.ingestion.ports.out.sink

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.LogSinkAdapter

class LogSink(
    private val logSinkAdapter: LogSinkAdapter
) {
    fun sendLogBatch(logBatchDTO: LogBatchDTO) {
        logSinkAdapter.sendBatch(logBatchDTO)
    }
}
