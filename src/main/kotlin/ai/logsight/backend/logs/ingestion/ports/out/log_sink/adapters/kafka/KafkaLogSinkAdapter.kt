package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.kafka

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.LogSinkAdapter
import org.springframework.stereotype.Component

@Component
class KafkaLogSinkAdapter : LogSinkAdapter {
    private val logger = LoggerImpl(KafkaLogSinkAdapter::class.java)

    override fun sendBatch(logBatchDTO: LogBatchDTO) {
    }
}
