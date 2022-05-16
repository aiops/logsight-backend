package ai.logsight.backend.logs.ingestion.ports.out.log_sink.adapters.queued_zmq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.stream.queue")
@ConstructorBinding
@EnableConfigurationProperties
class BlockingQueueConfigProperties(
    val maxSize: Int
)
