package com.loxbear.logsight.repositories.elasticsearch

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString

@Repository
class QualityRepository {

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String? = null

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

    fun getLogQualityData(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/log_quality_data.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getLogQualityOverview(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/log_quality_overview.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }


}