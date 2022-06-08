package ai.logsight.backend.charts.domain.query

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.users.domain.User

class GetChartDataQuery(
    val chartConfig: ChartConfig,
    val user: User,
    val credentials: Credentials
)
