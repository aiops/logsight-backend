package ai.logsight.backend.common.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(ElasticsearchConfigProperties::class)
class RestClientConfig(private val esConfig: ElasticsearchConfigProperties) {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().basicAuthentication("elastic", "elasticsearchpassword").build()
    }
}
