package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.domain.service.Log

interface LogStream {
    fun send(serializedLogs: Collection<String>)
    fun serializeAndSend(topic: String, logs: Collection<Log>)
}
