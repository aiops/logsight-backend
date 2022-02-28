package ai.logsight.backend.logs.ingestion.ports.out.stream

import ai.logsight.backend.logs.domain.LogsightLog

interface LogStream {
    fun send(serializedLogs: Collection<String>): Int
    fun serializeAndSend(topic: String, logsightLogs: Collection<LogsightLog>): Int
}
