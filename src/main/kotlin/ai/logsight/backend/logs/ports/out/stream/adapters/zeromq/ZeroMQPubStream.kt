package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.domain.service.dto.Log
import ai.logsight.backend.logs.ports.out.stream.LogStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class ZeroMQPubStream(
    @Qualifier("pub") val zeroMqPubSocket: ZMQ.Socket
) : LogStream {

    override fun send(topic: String, logs: Collection<Log>) {
        val objectMapper = ObjectMapper()
        val logsSerialized: List<String> = logs.map { log ->
            "$topic ${objectMapper.writeValueAsString(log)}"
        }

        logsSerialized.forEach { log ->
            zeroMqPubSocket.send(log)
        }
    }
}
