package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.domain.charts.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.config.ElasticsearchConfigProperties
import com.loxbear.logsight.charts.elasticsearch.LineChartData
import org.json.JSONObject
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Repository
class ESChartRepository(val elasticsearchConfig: ElasticsearchConfigProperties) {
    fun getData(getDataQuery: GetChartDataQuery): String {
        val query = ESQueryBuilder().buildQuery(
            startTime = getDataQuery.startTime,
            stopTime = getDataQuery.stopTime,
            featureType = getDataQuery.feature,
            chartType = getDataQuery.chartType
        )
        val request = ESUtils.createElasticSearchRequestWithHeaders(query)
        val restTemplate: RestTemplate =
            RestTemplateBuilder().basicAuthentication(getDataQuery.username, getDataQuery.password).build()

        val url = "http://${elasticsearchConfig.address}/${getDataQuery.index}/_search"
        return restTemplate.postForEntity<String>(url, request).body!!
    }
}
