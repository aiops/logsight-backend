package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.sink.Sink
import ai.logsight.backend.logs.ingestion.ports.out.sink.serializers.LogBatchSerializer
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class ZeroMqSink(
    val logStreamZeroMqSocket: ZMQ.Socket,
    val logBatchSerializer: LogBatchSerializer,
) : Sink {
    private val logger = LoggerImpl(ApplicationLifecycleServiceImpl::class.java)

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        logger.debug("sending message $logBatchDTO to host ${logStreamZeroMqSocket.lastEndpoint}")
        sendString(logBatchSerializer.serialize(logBatchDTO))
    }

    override fun sendString(content: String) {
        logStreamZeroMqSocket.send(content)
    }
}
