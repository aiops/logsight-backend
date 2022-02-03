package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO

interface LogStream {
    fun sendBatch(batch: LogBatchDTO)
}
