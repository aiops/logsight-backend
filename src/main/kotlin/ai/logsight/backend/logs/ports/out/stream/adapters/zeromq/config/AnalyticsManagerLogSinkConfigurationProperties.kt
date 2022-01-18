package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "logsight.manager.log-stream")
@ConstructorBinding
class AnalyticsManagerLogSinkConfigurationProperties(
    val protocol: String = "tcp",
    val host: String = "localhost",
    val port: Int = 5321
)