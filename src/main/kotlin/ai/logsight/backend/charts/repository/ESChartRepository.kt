package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.RestTemplateConnector
import ai.logsight.backend.elasticsearch.config.ElasticsearchConfigProperties
import org.springframework.stereotype.Repository
import org.springframework.web.util.UriComponentsBuilder

@Repository
class ESChartRepository(val elasticsearchConfig: ElasticsearchConfigProperties) {
    private val connector = RestTemplateConnector()

    fun getData(getDataQuery: GetChartDataQuery, applicationIndices: String): String {
        val chartConfig = getDataQuery.chartConfig
        val query = ESQueryBuilder().buildQuery(
            startTime = chartConfig.startTime,
            stopTime = chartConfig.stopTime,
            featureType = chartConfig.feature,
            chartType = chartConfig.type,
            timeZone = chartConfig.timeZone
        )
        val url = UriComponentsBuilder.newInstance().scheme(elasticsearchConfig.protocol).host(elasticsearchConfig.host)
            .port(elasticsearchConfig.port).path(applicationIndices).path("/_search").build().toString()
        return connector.sendRequest(
            url, Credentials(getDataQuery.credentials.username, getDataQuery.credentials.password), query
        )
    }
}
