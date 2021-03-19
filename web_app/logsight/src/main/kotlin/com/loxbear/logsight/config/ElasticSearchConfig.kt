package com.loxbear.logsight.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackages = ["com.loxbear.logsight.repositories.elasticsearch"])
@ComponentScan(basePackages = ["com.loxbear.logsight"])
class ElasticsearchClientConfig : AbstractElasticsearchConfiguration() {
    // TODO: change to http://localhost:9200

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String? = null

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration
            .builder()
            .connectedTo(elasticsearchUrl)
            .build()
        return RestClients.create(clientConfiguration).rest()
    }
}