package ai.logsight.backend.logs.ingestion.ports.out.stream.config

import ai.logsight.backend.logs.ingestion.ports.out.stream.LogQueue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LogQueueConfig(
    private val logQueueConfigProperties: LogQueueConfigProperties
) {
    @Bean
    fun logQueue(): LogQueue =
        LogQueue(logQueueConfigProperties.maxSize)
}
