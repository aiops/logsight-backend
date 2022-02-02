package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.manager.log-stream")
@ConstructorBinding
class ZeroMQConfigurationProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val port: Int = 5555
)
