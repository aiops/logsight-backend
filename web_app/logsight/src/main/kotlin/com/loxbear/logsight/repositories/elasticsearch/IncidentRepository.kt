package com.loxbear.logsight.repositories.elasticsearch

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService.Companion.readFileAsString

@Repository
class IncidentRepository {
    val restTemplate = RestTemplate()

    fun getTopKIncidentData(esIndexUserApp: String, startTime: String, stopTime: String): String {
        //val jsonString: String = readFileAsString("src/main/resources/queries/top_incidents_dashboard_request.json")
        val jsonString: String = readFileAsString("queries/top_incidents_dashboard_request.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)

        return restTemplate.postForEntity<String>("http://elasticsearch:9200/$esIndexUserApp/_search", request).body!!
    }
}