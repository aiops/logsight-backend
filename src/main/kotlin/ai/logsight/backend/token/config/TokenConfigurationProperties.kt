package ai.logsight.backend.token.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.token")
class TokenConfigurationProperties {
    var minutes: Long = 15
    var duration: Duration = Duration.ofMinutes(minutes)
}
