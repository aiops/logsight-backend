package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.RestTemplateConnector
import org.springframework.stereotype.Repository

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
        val url =
            "${elasticsearchConfig.protocol}://${elasticsearchConfig.address}/${getDataQuery.dataSource.index}/_search" // Do it with URL BUILDER
        val connector = RestTemplateConnector(url, getDataQuery.credentials.username, getDataQuery.credentials.password)
        return connector.sendRequest(query)
    }
}
