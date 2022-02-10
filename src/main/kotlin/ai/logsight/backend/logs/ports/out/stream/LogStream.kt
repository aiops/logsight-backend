package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.domain.service.Log

interface LogStream {
    fun send(serializedLogs: Collection<String>): Int
    fun serializeAndSend(topic: String, logs: Collection<Log>): Int
}
