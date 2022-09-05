package ai.logsight.backend.compare.limit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.limit.tags")
@EnableConfigurationProperties
data class CompareLimitConfigProperties(
    var corporate: Long = 10,
    var freemium: Long = 10,
    var developer: Long = 10
)
