package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
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

    fun getTemplates(esIndexUserApp: String, startTime: String, stopTime: String, search: String?): String {

        val jsonString = if (search != null) {
            readFileAsString("src/main/resources/queries/variable_analysis_search.json")
        } else {
            readFileAsString("src/main/resources/queries/variable_analysis.json")
        }
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("search_param", search ?: "")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity(json.toString(), headers)
        return restTemplate.postForEntity<String>("http://localhost:9200/$esIndexUserApp/_search", request).body!!
    }

    fun getSpecificTemplate(esIndexUserApp: String, startTime: String, stopTime: String, template: String, param: String, paramValue: String): String {
        val jsonString = readFileAsString("src/main/resources/queries/variable_analysis_specific_template.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("query_template", template)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity(json.toString(), headers)
        return restTemplate.postForEntity<String>("http://localhost:9200/$esIndexUserApp/_search", request).body!!
    }

    fun getSpecificTemplateDifferentParams(esIndexUserApp: String, startTime: String, stopTime: String, template: String, param: String, paramValue: String): LineChartData {
        val jsonString = readFileAsString("src/main/resources/queries/variable_analysis_specific_template_grouped_stacked.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("query_template", template).replace("param_name", param)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity(json.toString(), headers)
        return restTemplate.postForEntity<LineChartData>("http://localhost:9200/$esIndexUserApp/_search", request).body!!
    }
}