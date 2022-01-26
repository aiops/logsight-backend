package ai.logsight.backend.charts.domain.query

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.Credentials
import ai.logsight.backend.charts.domain.dto.DataSourceConfig

class GetChartDataQuery(
    val chartConfig: ChartConfig,
    val dataSource: DataSourceConfig,
    val applicationId: Long,
    val credentials: Credentials
)
