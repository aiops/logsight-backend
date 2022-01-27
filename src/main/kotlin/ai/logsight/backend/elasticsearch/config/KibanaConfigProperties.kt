package ai.logsight.backend.elasticsearch.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.kibana")
data class KibanaConfigProperties(
    var host: String,
    var port: String,
    var header: String = "kbn-xsrf",
    var protocol: String = "http"
)
