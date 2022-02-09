package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.domain.service.Log
import ai.logsight.backend.logs.ports.out.stream.LogStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class LogStreamZeroMq(
    val logStreamZeroMqSocket: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
) : LogStream {

    override fun send(serializedLogs: Collection<String>) = serializedLogs.forEach { log ->
        logStreamZeroMqSocket.send(log)
    }

    override fun serializeAndSend(topic: String, logs: Collection<Log>) =
        send(serializer.serialize(topic, logs))
}

@Component
class TopicJsonSerializer(
    val objectMapper: ObjectMapper = ObjectMapper()
) {
    fun serialize(topic: String, logs: Collection<Log>): Collection<String> =
        logs.map { log -> "$topic ${objectMapper.writeValueAsString(log)}" }

    fun deserialize(logs: Collection<String>): Collection<Log> = logs.map { serializedLog ->
        val logStr = serializedLog.split(" ", limit = 2)[1]
        objectMapper.readValue(logStr, Log::class.java)
    }
}
