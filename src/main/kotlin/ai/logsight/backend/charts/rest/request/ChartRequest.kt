package ai.logsight.backend.charts.rest.request

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.DataSourceConfig
import java.util.*

data class ChartRequest(
    val chartConfig: ChartConfig,
    val applicationId: UUID?,
)
