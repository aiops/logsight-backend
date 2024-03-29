package ai.logsight.backend.connectors.sink.zmq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.connectors.zeromq")
@ConstructorBinding
@EnableConfigurationProperties
data class ZmqConfigProperties(
    val protocol: String,
    val host: String,
    val port: Int,
    val hwm: Int
)
