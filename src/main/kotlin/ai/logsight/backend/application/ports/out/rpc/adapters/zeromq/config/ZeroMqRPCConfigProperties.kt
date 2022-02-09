package ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.application-rpc.zeromq")
@ConstructorBinding
class ZeroMqRPCConfigProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val port: Int = 5555,
    val timeout: Int = 5 * 1000 // seconds
)
