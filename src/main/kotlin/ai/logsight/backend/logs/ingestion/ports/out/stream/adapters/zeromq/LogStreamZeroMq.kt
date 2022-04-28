package ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.common.logging.LoggerImpl
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
    private val logger = LoggerImpl(ApplicationLifecycleServiceImpl::class.java)

    override fun send(serializedLog: String): Boolean {
        logger.debug("sending message $serializedLog to host ${logStreamZeroMqSocket.lastEndpoint}")
        return logStreamZeroMqSocket.send(serializedLog)
    }

    override fun serializeAndSendAll(topic: String, logsightLog: LogsightLog): Boolean =
        send(serializer.serialize(topic, logsightLog))
}
