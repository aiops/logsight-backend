package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.incidents.data.TopKIncidentsTable

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

    fun getTopKIncidentData(): TopKIncidentsTable {

        val jsonString: String = readFileAsString("src/main/resources/queries/system_overview_heatmap_request.json")
//        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
//        val index = "1234-213_app_name_test1_log_ad"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(jsonString)

        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)
        return restTemplate.postForEntity<TopKIncidentsTable>("http://localhost:9200/_search", request).body!!
    }



}