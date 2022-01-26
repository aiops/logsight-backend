package ai.logsight.backend.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.elasticsearch")
data class ElasticsearchConfigProperties(
    var username: String = "",
    var password: String = "",
    var address: String = "localhost:9200"
)
