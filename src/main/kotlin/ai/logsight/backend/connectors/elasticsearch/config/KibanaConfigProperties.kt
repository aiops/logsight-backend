package ai.logsight.backend.connectors.elasticsearch.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.kibana")
@EnableConfigurationProperties
data class KibanaConfigProperties(
    val scheme: String,
    val host: String,
    val port: String,
    val header: String
)
