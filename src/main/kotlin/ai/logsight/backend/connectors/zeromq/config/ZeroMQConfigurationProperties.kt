package ai.logsight.backend.connectors.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "logsight.manager.log-stream")
@ConstructorBinding
class ZeroMQConfigurationProperties(
    val protocol: String = "tcp",
    val host: String = "0.0.0.0",
    val reqPort: Int = 5555,
    val pubPort: Int = 5559,
    val reqTimeout: Int = 5 * 1000 // seconds
)
