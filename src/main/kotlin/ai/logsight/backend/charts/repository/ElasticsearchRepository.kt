package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.builders.ESQueryBuilder
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.exceptions.ElasticsearchException
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.users.ports.out.external.ExternalService
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import org.springframework.stereotype.Repository
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder

@Repository
class ElasticsearchRepository(val elasticsearchConfig: ElasticsearchConfigProperties, val externalServices: ExternalServiceManager,) {
    private val connector = RestTemplateConnector()

    fun getData(getDataQuery: GetChartDataQuery, applicationIndices: String): String {
        val chartConfig = getDataQuery.chartConfig
        val query = ESQueryBuilder().buildQuery(
            chartConfig.parameters as Map<String, String>
        )
        val url = UriComponentsBuilder.newInstance().scheme(elasticsearchConfig.scheme).host(elasticsearchConfig.host)
            .port(elasticsearchConfig.port).path(applicationIndices).path("/_search").build().toString()
        return try {
            connector.sendRequest(
                url, Credentials(getDataQuery.user.email, getDataQuery.user.key), query
            ).body!!
        } catch (e: HttpClientErrorException.Forbidden){
            externalServices.initializeServicesForUser(getDataQuery.user)
            connector.sendRequest(
                url, Credentials(getDataQuery.user.email, getDataQuery.user.key), query
            ).body!!
        }
    }
}
