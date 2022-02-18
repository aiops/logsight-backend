package ai.logsight.backend.connectors.elasticsearch.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration

@Configuration
class ElasticsearchClientConfig(
    val elasticsearchConfigProperties: ElasticsearchConfigProperties
) : AbstractElasticsearchConfiguration() {
    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("${elasticsearchConfigProperties.host}:${elasticsearchConfigProperties.port}").withBasicAuth(
                elasticsearchConfigProperties.credentials.username, elasticsearchConfigProperties.credentials.password
            ).build()
        return RestClients.create(clientConfiguration).rest()
    }
}
