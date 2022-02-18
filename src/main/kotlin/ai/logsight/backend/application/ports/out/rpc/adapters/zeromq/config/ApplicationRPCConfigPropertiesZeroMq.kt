package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "logsight.application-rpc-zeromq")
@ConstructorBinding
@EnableConfigurationProperties
data class ApplicationRPCConfigPropertiesZeroMq(
    val protocol: String,
    val host: String,
    val port: Int,
    val timeout: Int // in seconds
)
