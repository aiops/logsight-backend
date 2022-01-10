package ai.logsight.backend.token.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "logsight.token.activation")
class ActivationTokenConfigurationProperties(val tokenDuration: Duration)
