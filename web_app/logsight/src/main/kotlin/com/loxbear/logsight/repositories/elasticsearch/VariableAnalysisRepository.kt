package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LogLevelPieChartData

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService.Companion.readFileAsString

@Repository
class VariableAnalysisRepository {
    val restTemplate = RestTemplate()

    fun getTemplates(es_index_user_app: String, startTime: String, stopTime: String): String {

        val jsonString: String = readFileAsString("src/main/resources/queries/variable_analysis.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity(json.toString(), headers)
        return restTemplate.postForEntity<String>("http://localhost:9200/$es_index_user_app/_search", request).body!!
    }
}