package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.domain.service.dto.Log

interface LogStream {
    fun send(serializedLogs: Collection<String>)
    fun serialize(topic: String, logs: Collection<Log>): Collection<String>
    fun serializeAndSend(topic: String, logs: Collection<Log>)
}
