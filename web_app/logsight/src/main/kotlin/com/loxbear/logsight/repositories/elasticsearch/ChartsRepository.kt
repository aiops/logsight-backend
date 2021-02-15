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
class ChartsRepository {
    val restTemplate = RestTemplate()

    fun getLineChartData(): LineChartData {
        val jsonString: String = readFileAsString("src/main/resources/queries/first_plot_request.json")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val json = JSONObject(jsonString)
        val request: HttpEntity<String> = HttpEntity<String>(json.toString(), headers)

        return restTemplate.postForEntity<LineChartData>("http://localhost:9200/_search", request).body!!
    }
}