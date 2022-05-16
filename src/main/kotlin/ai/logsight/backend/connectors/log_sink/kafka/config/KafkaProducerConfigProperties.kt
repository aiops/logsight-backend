package ai.logsight.backend.connectors.log_sink.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.stream.kafka")
@ConstructorBinding
@EnableConfigurationProperties
class KafkaProducerConfigProperties(
    val bootstrapServer: String,
    val topic: String
)
