package ai.logsight.backend.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.elasticsearch")
data class ElasticsearchConfigProperties(
    var username: String = "elastic", // TODO: THIS WAS ONLY FOR TESTING REMOVE
    var password: String = "elasticsearchpassword", // todo: this was only for testing remove
    var address: String = "localhost:9200",
    var host: String = "localhost",
    var port: String = "9200"
)
