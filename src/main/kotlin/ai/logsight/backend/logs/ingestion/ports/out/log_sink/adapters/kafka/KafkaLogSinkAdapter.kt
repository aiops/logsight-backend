package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.kafka

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.log_sink.kafka.KafkaProducerConnector
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.LogSinkAdapter
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.serializer.LogBatchSerializer
import org.springframework.stereotype.Component

@Component
class KafkaLogSinkAdapter(
    val kafkaProducerConnector: KafkaProducerConnector,
    val jsonLogBatchSerializer: LogBatchSerializer,
) : LogSinkAdapter {

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        val logBatchDTOJsonString = jsonLogBatchSerializer.serialize(logBatchDTO)
            ?: throw LogSinkException("Serialization error: Failed to process log batch $logBatchDTO.")

        if (!kafkaProducerConnector.send(logBatchDTOJsonString)) {
            throw LogSinkException("Transmission error: Failed to process log batch $logBatchDTO.")
        }
    }
}
