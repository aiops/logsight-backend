package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.log-stream.zeromq")
@ConstructorBinding
class LogStreamZeroMqConfigProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val port: Int = 5559,
)
