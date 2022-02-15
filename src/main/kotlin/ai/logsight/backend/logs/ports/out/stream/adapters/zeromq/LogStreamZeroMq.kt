package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.service.Log
import ai.logsight.backend.logs.ports.out.stream.LogStream
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class LogStreamZeroMq(
    val logStreamZeroMqSocket: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
) : LogStream {

    override fun send(serializedLogs: Collection<String>): Int = serializedLogs.map { log ->
        if (logStreamZeroMqSocket.send(log)) 1 else 0
    }.sum()

    override fun serializeAndSend(topic: String, logs: Collection<Log>): Int =
        send(logs.map { serializer.serialize(topic, it) })
}
