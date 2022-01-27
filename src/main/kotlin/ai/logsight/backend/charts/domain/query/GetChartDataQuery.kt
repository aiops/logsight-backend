package ai.logsight.backend.charts.domain.query

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.DataSourceConfig
import ai.logsight.backend.common.dto.Credentials

class GetChartDataQuery(
    val chartConfig: ChartConfig,
    val dataSource: DataSourceConfig,
    val applicationId: Long,
    val credentials: Credentials
)
