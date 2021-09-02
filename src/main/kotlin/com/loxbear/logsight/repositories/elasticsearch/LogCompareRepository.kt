package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository
import org.springframework.web.client.postForEntity
import utils.UtilsService

@Repository
class LogCompareRepository {

    @Value("\${elasticsearch.url}")
    private val elasticsearchUrl: String? = null

    @Value("\${resources.path}")
    private val resourcesPath: String = ""

    fun getApplicationVersions(esIndexUserApp: String, userKey: String): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/application_versions.json").path
        val jsonRequest: String = UtilsService.readFileAsString(path)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getLogCountBar(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        intervalAggregate: String,
        tag: String
    ): String? {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString: String = UtilsService.readFileAsString("${resourcesPath}queries/compare_bar_plot.json")
        val jsonRequest = jsonString.replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("interval_aggregate", intervalAggregate)
            .replace("tag_label", tag)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getAnomaliesBarChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        intervalAggregate: String,
        tag: String
    ): LineChartData {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString = UtilsService.readFileAsString("${resourcesPath}queries/log_compare_anomalies_barplot.json")
        val jsonRequest = jsonString.replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("interval_aggregate", intervalAggregate)
            .replace("tag_label", tag)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)

        return restTemplate.postForEntity<LineChartData>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }

    fun getCompareTemplatesHorizontalBar(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        intervalAggregate: String,
        baselineTagId: String,
        compareTagId: String
    ): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val jsonString = UtilsService.readFileAsString("${resourcesPath}queries/log_compare_anomalies_barplot_horizontal.json")
        val jsonRequest = jsonString
            .replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("baseline", baselineTagId)
            .replace("compare", compareTagId)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$es_index_user_app/_search", request).body!!
    }

    fun getLogCompareData(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        baselineTagId: String,
        compareTagId: String
    ): String {
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(userKey, "test-test")
            .build();
        val path = ClassPathResource("${resourcesPath}queries/log_compare_data.json").path
        val jsonString: String = UtilsService.readFileAsString(path)
        val jsonRequest = jsonString
            .replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("compare_tag_label", compareTagId)
            .replace("baseline_tag_label", baselineTagId)

        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("http://$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }


}