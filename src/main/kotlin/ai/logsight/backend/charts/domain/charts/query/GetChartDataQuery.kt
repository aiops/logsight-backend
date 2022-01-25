package ai.logsight.backend.charts.domain.charts.query

import java.util.UUID

data class GetChartDataQuery(
    val chartType: String,
    val startTime: String,
    val stopTime: String,
    val feature: String,
    val applicationId: String,
    val index: String,
    val username: String,
    val password: String
)
