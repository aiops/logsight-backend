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
class IncidentRepository {
//    val restTemplate = RestTemplateBuilder()
//        .basicAuthentication("elastic", "elasticsearchpassword")
//        .build();

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String? = null

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

    fun getTopKIncidentData(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/top_incidents_dashboard_request.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getIncidentsBarChartData(esIndexUserApp: String, startTime: String, stopTime: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/incidents_bar_chart_data_request.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getIncidentsTableData(applicationsIndexes: String, startTime: String, stopTime: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/incidents-table-request.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$applicationsIndexes/_search", request).body!!
    }
}