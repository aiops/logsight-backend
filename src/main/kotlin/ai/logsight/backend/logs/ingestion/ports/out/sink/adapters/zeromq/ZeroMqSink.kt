package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.sink.Sink
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class ZeroMqSink(
    val logStreamZeroMqSocket: ZMQ.Socket,
    val serializer: TopicJsonSerializer,
) : Sink {
    private val logger = LoggerImpl(ApplicationLifecycleServiceImpl::class.java)

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        logger.debug("sending message $logBatchDTO to host ${logStreamZeroMqSocket.lastEndpoint}")
        sendString(serializer.serialize(logBatchDTO))
    }

    override fun sendString(content: String) {
        logStreamZeroMqSocket.send(content)
    }
}
