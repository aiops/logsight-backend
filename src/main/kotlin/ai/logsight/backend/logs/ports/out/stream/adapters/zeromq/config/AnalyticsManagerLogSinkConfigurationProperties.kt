package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "logsight.manager.log-stream")
@ConstructorBinding
class AnalyticsManagerLogSinkConfigurationProperties(
    val protocol: String = "tcp",
    val host: String = "127.0.0.1",
    val port: Int = 5555
)
