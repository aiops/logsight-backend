package ai.logsight.backend.charts.domain.dto

data class ChartConfig(
    var type: String = "",
    val startTime: String,
    val stopTime: String,
    val feature: String,
    val indexType: String
)
