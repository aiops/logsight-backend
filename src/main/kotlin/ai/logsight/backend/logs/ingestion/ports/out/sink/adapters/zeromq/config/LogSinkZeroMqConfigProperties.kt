package ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.stream.zeromq")
@ConstructorBinding
@EnableConfigurationProperties
data class LogSinkZeroMqConfigProperties(
    val protocol: String,
    val host: String,
    val port: Int,
    val hwm: Int
)
