package ai.logsight.backend.charts.repository

import ai.logsight.backend.charts.ESChartsServiceImpl
import ai.logsight.backend.charts.domain.charts.query.GetChartDataQuery
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
    fun `should return heatmap data for system overview`() {
        // given
        val query = GetChartDataQuery(
            "heatmap",
            "2016-07-15T15:29:50+02:00",
            "2022-07-15T15:29:50+02:00",
            "system_overview",
            "gmmlbirrlud46szjhax99imhok_jbossjson",
            "gmmlbirrlud46szjhax99imhok_jbossjson_log_ad",
            "elastic",
            "elasticsearchpassword"
        )
        // when
        val data = repository.getData(query)
        // then
        println(data)
    }

    @Test
    fun `should work with services`() {
        // given
        val query = GetChartDataQuery(
            "heatmap",
            "2016-07-15T15:29:50+02:00",
            "2022-07-15T15:29:50+02:00",
            "system_overview",
            "gmmlbirrlud46szjhax99imhok_jbossjson",
            "gmmlbirrlud46szjhax99imhok_jbossjson_log_ad",
            "elastic",
            "elasticsearchpassword"
        )
        // when
        val data = service.createHeatmap(query)
        // then
        println(data)
        // then
    }
}
