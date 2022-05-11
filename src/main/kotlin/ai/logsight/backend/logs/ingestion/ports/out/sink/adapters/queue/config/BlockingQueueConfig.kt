package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class BlockingQueueConfig(
    private val logQueueConfigProperties: BlockingQueueConfigProperties
) {
    @Bean
    fun blockingLogQueue(): LinkedBlockingQueue<String> =
        LinkedBlockingQueue(logQueueConfigProperties.maxSize)
}
