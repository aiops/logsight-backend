package ai.logsight.backend.limiter.verifications.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.limit.verification")
@EnableConfigurationProperties
data class VerificationLimitConfigProperties(
    var corporate: Long,
    var freemium: Long,
    var developer: Long
)
