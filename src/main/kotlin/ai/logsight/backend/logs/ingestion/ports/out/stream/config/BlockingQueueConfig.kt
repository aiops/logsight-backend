package ai.logsight.backend.logs.ingestion.ports.out.stream.config

import ai.logsight.backend.logs.domain.LogsightLog
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class BlockingQueueConfig(
    private val logQueueConfigProperties: BlockingQueueConfigProperties
) {
    @Bean
    fun blockingLogQueue(): LinkedBlockingQueue<Pair<String, LogsightLog>> =
        LinkedBlockingQueue(logQueueConfigProperties.maxSize)
}
