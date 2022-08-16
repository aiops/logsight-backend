package ai.logsight.backend.connectors.elasticsearch

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.connectors.elasticsearch.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.elasticsearch.config.KibanaConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.elasticsearch.client.RestHighLevelClient
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito.spy
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

internal class ElasticsearchServiceUnitTest {
    private val client: RestHighLevelClient = mockk(relaxed = true)
    private val kibanaConfig: KibanaConfigProperties = mockk()
    private val elasticsearchConfig: ElasticsearchConfigProperties = ElasticsearchConfigProperties("http", "localhost", "9200", Credentials(TestInputConfig.baseUser.email, TestInputConfig.baseUser.key))
    private val restConnector: RestTemplateConnector = mockk()
    private val elasticsearchService = ElasticsearchService(client, kibanaConfig, elasticsearchConfig, restConnector)
    private val spyElasticsearchService = spy(elasticsearchService)

    @Test
    fun `should return data for query`() {
        // given
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "compare_id",
                    "indexType" to "verifications",
                )
            )
        )
        val getChartDataQuery = GetChartDataQuery(
            chartConfig = chartRequest.chartConfig,
            user = TestInputConfig.baseUser,
        )
        val header = HttpHeaders()
        header.contentType = MediaType.APPLICATION_JSON

        val responseEntity: ResponseEntity<String> = ResponseEntity<String>(
            "some response body",
            header,
            HttpStatus.OK
        )
        // when
        every { restConnector.sendRequest(any(), any(), any(), any()) } returns responseEntity
        val result = elasticsearchService.getData(getChartDataQuery, "${TestInputConfig.baseUser.key}_verifications")
        // then
        Assertions.assertEquals(result, responseEntity.body!!)
        verify(exactly = 1) { restConnector.sendRequest(any(), any(), any(), any()) }
    }

//    @Test
//    fun `should initESUser if http client forbidden exception is thrown`(){
//        //given
//        val chartRequest = ChartRequest(
//            chartConfig = ChartConfig(
//                mutableMapOf(
//                    "type" to "util",
//                    "feature" to "compare_id",
//                    "indexType" to "verifications",
//                )
//            )
//        )
//        val getChartDataQuery = GetChartDataQuery(
//            chartConfig = chartRequest.chartConfig,
//            user = TestInputConfig.baseUser,
//        )
//        val header = HttpHeaders()
//        header.contentType = MediaType.APPLICATION_JSON
//
//        val responseEntity: ResponseEntity<String> = ResponseEntity<String>(
//            "some response body",
//            header,
//            HttpStatus.OK
//        )
//        //when
//        every { restConnector.sendRequest(any(), any(), any(), any()) } throws HttpClientErrorException.Forbidden.create(HttpStatus.FORBIDDEN, "tex",header, ByteArray(1), null)
//       `when` (spyElasticsearchService.createESUser(TestInputConfig.baseUser.email, TestInputConfig.baseUser.key,TestInputConfig.baseUser.key ))
//        val result = spyElasticsearchService.getData(getChartDataQuery, "${TestInputConfig.baseUser.key}_verifications")
//        //then
//        verify(exactly = 1) { spyElasticsearchService.initESUser(any(), any())}
//    }
}
