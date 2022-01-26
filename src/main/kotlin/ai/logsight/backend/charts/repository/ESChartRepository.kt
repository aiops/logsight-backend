package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.config.ElasticsearchConfigProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Repository
class ESChartRepository(val elasticsearchConfig: ElasticsearchConfigProperties) {
    fun getData(getDataQuery: GetChartDataQuery): String {
        val chartConfig = getDataQuery.chartConfig
        val query = ESQueryBuilder().buildQuery(
            startTime = chartConfig.startTime,
            stopTime = chartConfig.stopTime,
            featureType = chartConfig.feature,
            chartType = chartConfig.type
        )
        val request = ESUtils.createElasticSearchRequestWithHeaders(query)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            getDataQuery.credentials.username, getDataQuery.credentials.password
        ).build()

        val url = "https://${elasticsearchConfig.address}/${getDataQuery.dataSource.index}/_search"
        return restTemplate.postForEntity<String>(url, request).body!!
    }
}
