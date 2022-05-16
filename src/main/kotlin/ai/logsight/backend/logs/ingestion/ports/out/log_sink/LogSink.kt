package ai.logsight.backend.logs.ingestion.ports.out.log_sink

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.LogSinkAdapter

class LogSink(
    private val logSinkAdapter: LogSinkAdapter
) {
    fun sendLogBatch(logBatchDTO: LogBatchDTO) {
        logSinkAdapter.sendBatch(logBatchDTO)
    }
}
