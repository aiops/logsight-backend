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

    override fun send(serializedLogs: Collection<String>) = serializedLogs.forEach { log ->
        zeroMqPubSocket.send(log)
    }

    override fun serialize(topic: String, logs: Collection<Log>): Collection<String> = logs.map { log ->
        val objectMapper = ObjectMapper()
        "$topic ${objectMapper.writeValueAsString(log)}"
    }

    override fun serializeAndSend(topic: String, logs: Collection<Log>) =
        send(serialize(topic, logs))
}
