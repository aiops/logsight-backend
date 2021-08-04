package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
import com.loxbear.logsight.charts.elasticsearch.LogLevelPieChartData
import com.loxbear.logsight.charts.elasticsearch.SystemOverviewData

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.security.core.Authentication
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Repository
class ChartsRepository {
//    val restTemplate = RestTemplateBuilder()
//    .basicAuthentication("elastic", "elasticsearchpassword")
//    .build();
//    val restTemplate = RestTemplate()

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String = "localhost:9200"

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

//    fun getLineChartData(): LineChartData {
//        val jsonRequest: String = readFileAsString("${resourcesPath}queries/first_plot_request.json")
//        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
//        return restTemplate.postForEntity<LineChartData>("http://$elasticsearchUrl/1234-213_app_name_test_log_ad/_search", request).body!!
//    }

    fun getAnomaliesBarChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LineChartData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString = readFileAsString("${resourcesPath}queries/dashboard_anomalies_barplot.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

        return restTemplate.postForEntity<LineChartData>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }

    fun getLogLevelPieChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LogLevelPieChartData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString: String = readFileAsString("${resourcesPath}queries/log_level_pie_chart_request.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

        return restTemplate.postForEntity<LogLevelPieChartData>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }

    fun getLogLevelStackedLineChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LineChartData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString: String = readFileAsString("${resourcesPath}queries/log_level_stacked_line_chart_request.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<LineChartData>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }

    fun getSystemOverviewHeatmapChartData(esIndexUserAppLogAd: String,
                                          startTime: String, stopTime: String, userKey: String): SystemOverviewData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString: String = readFileAsString("${resourcesPath}queries/system_overview_heatmap_request.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<SystemOverviewData>("http://$elasticsearchUrl/$esIndexUserAppLogAd/_search", request).body!!
    }

    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}