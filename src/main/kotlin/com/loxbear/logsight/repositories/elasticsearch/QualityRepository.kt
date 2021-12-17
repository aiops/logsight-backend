package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.services.elasticsearch.ElasticsearchService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString

@Repository
class QualityRepository(
    val elasticsearchService: ElasticsearchService,
) {
    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    fun getLogQualityData(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String =
        elasticsearchService.execElasticsearchQuery(
            esIndexUserApp,
            startTime,
            stopTime,
            userKey,
            "${resourcesPath}queries/log_quality_data.json"
        )

    fun getLogQualityOverview(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String =
        elasticsearchService.execElasticsearchQuery(
            esIndexUserApp,
            startTime,
            stopTime,
            userKey,
            "${resourcesPath}queries/log_quality_overview.json"
        )
}