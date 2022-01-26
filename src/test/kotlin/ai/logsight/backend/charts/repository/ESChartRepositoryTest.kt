package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.ESChartsServiceImpl
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.Credentials
import ai.logsight.backend.charts.domain.dto.DataSourceConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.common.config.ElasticsearchConfigProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class ESChartRepositoryTest {
    @Autowired
    private val properties: ElasticsearchConfigProperties = ElasticsearchConfigProperties()
    private final val repository = ESChartRepository(properties)

    @Autowired
    private val service: ESChartsServiceImpl = ESChartsServiceImpl(repository)

    @Test
    fun `should work with services`() {
        // given
        val query = GetChartDataQuery(
            credentials = Credentials("elastic", "elasticsearchpassword"),
            chartConfig = ChartConfig("heatmap", "ngx", "now-2y", "now", "system_overview"),
            dataSource = DataSourceConfig("elasticsearch", "gmmlbirrlud46szjhax99imhok_jbossjson_log_ad"),
            applicationId = 5
        )
        // when
        val data = service.createHeatMap(query)
        // then
        println(data)
        // then
    }
}
