package ai.logsight.backend.charts.rest.request

import java.util.*

data class ChartRequest(
    val chartConfig: ChartConfig,
    val dataSource: DataSourceConfig,
    val applicationId: String,
    val feature: String
)

data class ChartConfig(
    var type: String = "",
    val library: String,
    val startTime: String,
    val stopTime: String
)

data class DataSourceConfig(
    val type: String,
    val index: String
)
