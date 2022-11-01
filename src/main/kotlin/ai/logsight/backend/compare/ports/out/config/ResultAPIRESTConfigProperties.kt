package ai.logsight.backend.compare.ports.out.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.result-api.rest")
@EnableConfigurationProperties
data class ResultAPIRESTConfigProperties(
    val scheme: String,
    val host: String,
    val port: Int,
    val comparePath: String,
    val autologPath: String
)
