package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
import com.loxbear.logsight.charts.elasticsearch.LogLevelPieChartData
import com.loxbear.logsight.charts.elasticsearch.SystemOverviewData

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService.Companion.readFileAsString

@Repository
class ChartsRepository {
    val restTemplate = RestTemplate()

    fun getLineChartData(): LineChartData {
        val jsonString: String = readFileAsString("src/main/resources/queries/first_plot_request.json")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(jsonString)
        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)

        return restTemplate.postForEntity<LineChartData>("http://localhost:9200/1234-213_app_name_test_log_ad/_search", request).body!!
    }

    fun getLogLevelPieChartData(es_index_user_app: String, startTime: String, stopTime: String): LogLevelPieChartData {

        val jsonString: String = readFileAsString("src/main/resources/queries/log_level_pie_chart_request.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)
        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)
        return restTemplate.postForEntity<LogLevelPieChartData>("http://localhost:9200/$es_index_user_app/_search", request).body!!
    }

    fun getLogLevelStackedLineChartData(es_index_user_app: String, startTime: String, stopTime: String): LineChartData {

        val jsonString: String = readFileAsString("src/main/resources/queries/log_level_stacked_line_chart_request.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)

        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)
        return restTemplate.postForEntity<LineChartData>("http://localhost:9200/$es_index_user_app/_search", request).body!!
    }

    fun getSystemOverviewHeatmapChartData(esIndexUserAppLogAd: String,
                                          startTime: String, stopTime: String): SystemOverviewData {

        val jsonString: String = readFileAsString("src/main/resources/queries/system_overview_heatmap_request.json")
        val timeJsonString = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val index = "1234-213_app_name_test1_log_ad"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(timeJsonString)

        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)
        return restTemplate.postForEntity<SystemOverviewData>("http://localhost:9200/$esIndexUserAppLogAd,$index/_search", request).body!!
    }



}