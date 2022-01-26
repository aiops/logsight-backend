package ai.logsight.backend.charts.domain.charts

import ai.logsight.backend.charts.domain.charts.models.ChartSeries

data class BarChart(
    val data: List<ChartSeries>
) : Chart
