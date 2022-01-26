package ai.logsight.backend.charts.domain.charts

import ai.logsight.backend.charts.domain.charts.models.ChartSeriesPoint

data class PieChart(
    val data: List<ChartSeriesPoint>
)
