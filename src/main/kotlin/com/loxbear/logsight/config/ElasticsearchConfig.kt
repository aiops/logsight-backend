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
class ElasticsearchConfig : AbstractElasticsearchConfiguration() {

    @Value("\${elasticsearch.url}")
    private lateinit var elasticsearchUrl: String
    @Value("\${elasticsearch.username}")
    private lateinit var username: String
    @Value("\${elasticsearch.password}")
    private lateinit var password: String

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration
            .builder()
            .connectedTo(elasticsearchUrl)
            .withBasicAuth(username, password)
            .build()
        return RestClients.create(clientConfiguration).rest()
    }
}