package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

// Redefine as LogSink
interface LogStream {
    fun send(serializedLog: String): Boolean
    fun serializeAndSend(logBatch: LogBatchDTO): Boolean
}
