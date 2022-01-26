package ai.logsight.backend.charts.rest.request

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.DataSourceConfig

data class ChartRequest(
    val chartConfig: ChartConfig,
    val dataSource: DataSourceConfig,
    val applicationId: String,
)
