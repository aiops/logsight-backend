package ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq

import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class LogStreamZeroMq(
    val logStreamZeroMqSocket: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
) : LogStream {

    override fun send(serializedLogs: Collection<String>): Int = serializedLogs.sumOf { log ->
        // The useless cast is needed due to an issue in kotlin: https://youtrack.jetbrains.com/issue/KT-46360
        if (logStreamZeroMqSocket.send(log)) 1 as Int else 0 as Int
    }

    override fun serializeAndSend(topic: String, logsightLogs: Collection<LogsightLog>): Int =
        send(logsightLogs.map { serializer.serialize(topic, it) })
}
