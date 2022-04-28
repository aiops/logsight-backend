package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.domain.LogsightLog

interface LogStream {
    fun send(serializedLog: String): Boolean
    fun serializeAndSendAll(topic: String, logsightLog: LogsightLog): Boolean
}
