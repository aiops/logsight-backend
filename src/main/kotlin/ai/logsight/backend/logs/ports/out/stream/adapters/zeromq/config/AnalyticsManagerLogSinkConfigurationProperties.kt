package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "logsight.manager.logs-stream")
@ConstructorBinding
@EnableConfigurationProperties
class AnalyticsManagerLogSinkConfigurationProperties(
    val protocol: String = "tcp",
    val host: String = "localhost",
    val port: Int = 5321
)
