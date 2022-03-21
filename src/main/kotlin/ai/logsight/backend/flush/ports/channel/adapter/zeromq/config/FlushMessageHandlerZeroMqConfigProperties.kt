package ai.logsight.backend.flush.ports.channel.adapter.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.control-rpc-in.zeromq")
@ConstructorBinding
@EnableConfigurationProperties
data class FlushMessageHandlerZeroMqConfigProperties(
    val protocol: String,
    val host: String,
    val port: Int,
    val topic: String
)
