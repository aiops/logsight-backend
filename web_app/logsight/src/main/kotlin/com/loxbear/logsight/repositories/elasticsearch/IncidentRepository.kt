package com.loxbear.logsight.repositories.elasticsearch

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString

@Repository
class IncidentRepository {
    val restTemplate = RestTemplate()

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String? = null

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

    fun getTopKIncidentData(esIndexUserApp: String, startTime: String, stopTime: String): String {
        val path = ClassPathResource("${resourcesPath}queries/top_incidents_dashboard_request.json").path
        val jsonString: String = readFileAsString(path)
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }
}