package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
import com.loxbear.logsight.charts.elasticsearch.LogLevelPieChartData
import com.loxbear.logsight.charts.elasticsearch.SystemOverviewData
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Repository
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Repository
class ChartsRepository(
    val userRepository: UserRepository,
) {

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String = ""

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

    fun getAnomaliesBarChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String
    ): LineChartData {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString = readFileAsString("${resourcesPath}queries/dashboard_anomalies_barplot.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

        return restTemplate.postForEntity<LineChartData>(
            "http://$elasticsearchUrl/$es_index_user_app/_search",
            request
        ).body!!
    }

    fun getNewTemplatesBarChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        baselineTagId: String?,
        compareTagId: String?,
        intervalAggregate: String
    ): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString = readFileAsString("${resourcesPath}queries/new_templates_compare_bar.json")
        val jsonRequest = jsonString
            .replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("baseline_tag_label", baselineTagId!!)
            .replace("compare_tag_label", compareTagId!!)
            .replace("interval_aggregate", intervalAggregate)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }


    fun getLogLevelPieChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String
    ): LogLevelPieChartData {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString: String = readFileAsString("${resourcesPath}queries/log_level_pie_chart_request.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

        return restTemplate.postForEntity<LogLevelPieChartData>(
            "http://$elasticsearchUrl/$es_index_user_app/_search",
            request
        ).body!!
    }

    fun getLogLevelStackedLineChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String
    ): LineChartData {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString: String = readFileAsString("${resourcesPath}queries/log_level_stacked_line_chart_request.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<LineChartData>(
            "http://$elasticsearchUrl/$es_index_user_app/_search",
            request
        ).body!!
    }

    fun getSystemOverviewHeatmapChartData(
        esIndexUserAppLogAd: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        compareTagId: String?,
        baselineTagId: String?,
        intervalAggregate: String?
    ): SystemOverviewData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        if (compareTagId == null && baselineTagId == null) {
            val jsonString: String = readFileAsString("${resourcesPath}queries/system_overview_heatmap_request.json")
            val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

            val returnData = restTemplate.postForEntity<SystemOverviewData>(
                "http://$elasticsearchUrl/$esIndexUserAppLogAd/_search",
                request
            ).body!!
            println(returnData)
            return returnData
        } else {
            val jsonString: String = readFileAsString("${resourcesPath}queries/heatmap_log_compare.json")
            val jsonRequest = jsonString
                .replace("start_time", startTime)
                .replace("stop_time", stopTime)
                .replace("baseline_tag_label", baselineTagId!!)
                .replace("compare_tag_label", compareTagId!!)
                .replace("interval_aggregate", intervalAggregate!!)
            val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
            return restTemplate.postForEntity<SystemOverviewData>(
                "http://$elasticsearchUrl/$esIndexUserAppLogAd/_search",
                request
            ).body!!
        }
    }

    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}