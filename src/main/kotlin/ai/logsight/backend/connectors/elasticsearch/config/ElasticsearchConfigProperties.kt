package ai.logsight.backend.connectors.elasticsearch.config

import ai.logsight.backend.common.dto.Credentials
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.elasticsearch")
data class ElasticsearchConfigProperties(
    @NestedConfigurationProperty
    var credentials: Credentials,
    var host: String,
    var port: String,
    var protocol: String = "http"
)
