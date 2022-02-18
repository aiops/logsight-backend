package ai.logsight.backend.compare.out.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.result-api.compare")
@EnableConfigurationProperties
data class CompareRESTConfigProperties(
    val scheme: String,
    val host: String,
    val port: Int,
    val path: String
)
