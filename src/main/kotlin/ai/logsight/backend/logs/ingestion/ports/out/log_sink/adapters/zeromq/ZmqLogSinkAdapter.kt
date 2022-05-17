package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.zeromq

import ai.logsight.backend.connectors.sink.zmq.ZmqSinkConnector
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.LogSinkAdapter
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.serializer.LogBatchSerializer
import org.springframework.stereotype.Component

@Component
class ZmqLogSinkAdapter(
    val zmqSinkConnector: ZmqSinkConnector,
    val jsonLogBatchSerializer: LogBatchSerializer,
) : LogSinkAdapter {

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        val logBatchDTOJsonString = jsonLogBatchSerializer.serialize(logBatchDTO)
            ?: throw LogSinkException("Serialization error: Failed to process log batch $logBatchDTO.")

        if (!zmqSinkConnector.send(logBatchDTOJsonString)) {
            throw LogSinkException("Transmission error: Failed to process log batch $logBatchDTO.")
        }
    }
}
