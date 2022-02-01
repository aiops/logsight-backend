package ai.logsight.backend.logs.ports.out.stream

import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.Log

interface LogStream {
    fun send(topic: String, logs: List<Log>)
}
