package ai.logsight.backend.charts.domain.query

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.dto.DataSourceConfig
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.users.domain.User

class GetChartDataQuery(
    val chartConfig: ChartConfig,
    val application: Application?,
    val user: User,
    val credentials: Credentials
)
