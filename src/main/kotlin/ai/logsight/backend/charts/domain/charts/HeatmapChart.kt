package ai.logsight.backend.charts.domain.charts

import ai.logsight.backend.charts.domain.charts.models.ChartSeries

data class HeatmapChart(
    val data: List<ChartSeries>
) : Chart
