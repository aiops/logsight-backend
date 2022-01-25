package ai.logsight.backend.token.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@ConfigurationProperties(prefix = "logsight.token")
@Component
class TokenConfigurationProperties {
    var duration: Duration = Duration.ofMinutes(15)
}
