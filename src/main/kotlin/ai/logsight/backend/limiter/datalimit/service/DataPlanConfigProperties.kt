package ai.logsight.backend.limiter.datalimit.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.limit.data")
@EnableConfigurationProperties
data class DataPlanConfigProperties(
    var corporate: Long,
    var freemium: Long,
    var developer: Long
)