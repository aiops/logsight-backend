package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.queued_zmq.config

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class BlockingQueueConfig(
    private val logQueueConfigProperties: BlockingQueueConfigProperties
) {
    @Bean
    fun blockingLogQueue(): LinkedBlockingQueue<LogBatchDTO> =
        LinkedBlockingQueue(logQueueConfigProperties.maxSize)
}
