package ai.logsight.backend.token.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(TokenConfigurationProperties::class)
@Configuration
class TokenConfiguration
