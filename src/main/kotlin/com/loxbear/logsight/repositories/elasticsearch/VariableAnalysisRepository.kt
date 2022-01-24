package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.LineChartData
import com.loxbear.logsight.charts.elasticsearch.LogLevelPieChartData
import com.loxbear.logsight.repositories.UserRepository
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import utils.UtilsService.Companion.readFileAsString

@Repository
class VariableAnalysisRepository(
    val userRepository: UserRepository,
) {
//    val restTemplate = RestTemplateBuilder()
//        .basicAuthentication("elastic", "elasticsearchpassword")
//        .build();

    @Value("\${elasticsearch.url}")
    private lateinit var elasticsearchUrl: String

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    fun getTemplates(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        intervalAggregate: String,
        search: String?,
        userKey: String
    ): String {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()

        val jsonString = if (search != null) {
            readFileAsString("${resourcesPath}queries/variable_analysis_search.json")
        } else {
            readFileAsString("${resourcesPath}queries/variable_analysis.json")
        }
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("interval_aggregate", intervalAggregate)
            .replace("search_param", search ?: "")
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getSpecificTemplate(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        intervalAggregate: String,
        template: String,
        param: String,
        paramValue: String,
        userKey: String
    ): String {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString = readFileAsString("${resourcesPath}queries/variable_analysis_specific_template.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("interval_aggregate", intervalAggregate)
            .replace("query_template", template)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }

    fun getSpecificTemplateDifferentParams(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        template: String,
        param: String,
        paramValue: String,
        userKey: String
    ): LineChartData {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString =
            readFileAsString("${resourcesPath}queries/variable_analysis_specific_template_grouped_stacked.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("query_template", template).replace("param_name", param)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<LineChartData>(
            "$elasticsearchUrl/$esIndexUserApp/_search",
            request
        ).body!!
    }

    fun getTopNTemplates(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        size: Int,
        userKey: String
    ): LogLevelPieChartData {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString = readFileAsString("${resourcesPath}queries/top_5_templates.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("template_size", size.toString())
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<LogLevelPieChartData>(
            "$elasticsearchUrl/$esIndexUserApp/_search",
            request
        ).body!!
    }

    fun getLogCountLineChart(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        userKey: String,
        intervalAggregate: String
    ): String? {
        val user = userRepository.findByKey(userKey).orElseThrow()
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user.email, user.key)
            .build()
        val jsonString: String = readFileAsString("${resourcesPath}queries/log_count_line_chart.json")
        val jsonRequest = jsonString.replace("start_time", startTime)
            .replace("stop_time", stopTime)
            .replace("interval_aggregate", intervalAggregate)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return restTemplate.postForEntity<String>("$elasticsearchUrl/$esIndexUserApp/_search", request).body!!
    }
}