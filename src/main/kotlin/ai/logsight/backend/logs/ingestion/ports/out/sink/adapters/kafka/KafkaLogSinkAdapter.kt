package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.kafka

import ai.logsight.backend.connectors.sink.kafka.KafkaProducerSinkConnector
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.exceptions.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.LogSinkAdapter
import ai.logsight.backend.logs.ingestion.ports.out.sink.serializer.LogBatchSerializer
import org.springframework.stereotype.Component

@Component
class KafkaLogSinkAdapter(
    val kafkaProducerSinkConnector: KafkaProducerSinkConnector,
    val jsonLogBatchSerializer: LogBatchSerializer,
) : LogSinkAdapter {

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
        val logBatchDTOJsonString = jsonLogBatchSerializer.serialize(logBatchDTO)
            ?: throw LogSinkException("Serialization error: Failed to process log batch $logBatchDTO.")

        if (!kafkaProducerSinkConnector.send(logBatchDTOJsonString)) {
            throw LogSinkException("Transmission error: Failed to process log batch $logBatchDTO.")
        }
    }
}
