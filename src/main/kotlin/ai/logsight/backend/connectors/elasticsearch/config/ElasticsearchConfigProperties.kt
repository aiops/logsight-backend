package ai.logsight.backend.connectors.elasticsearch.config

import ai.logsight.backend.common.dto.Credentials
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.elasticsearch")
@EnableConfigurationProperties
data class ElasticsearchConfigProperties(
    val scheme: String,
    val host: String,
    val port: String,
    @NestedConfigurationProperty val credentials: Credentials
)
