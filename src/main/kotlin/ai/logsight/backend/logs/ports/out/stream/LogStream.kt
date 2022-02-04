package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.domain.service.dto.Log

interface LogStream {
    fun send(topic: String, logs: Collection<Log>)
}
