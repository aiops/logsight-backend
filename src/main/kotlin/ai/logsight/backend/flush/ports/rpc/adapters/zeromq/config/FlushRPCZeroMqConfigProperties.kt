package ai.logsight.backend.flush.ports.rpc.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.logs.control-rpc-out.zeromq")
@ConstructorBinding
@EnableConfigurationProperties
data class FlushRPCZeroMqConfigProperties(
    val protocol: String,
    val host: String,
    val port: Int
)
