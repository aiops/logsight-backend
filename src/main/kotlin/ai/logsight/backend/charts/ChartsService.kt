package ai.logsight.backend.charts

import ai.logsight.backend.charts.domain.charts.BarChart
import ai.logsight.backend.charts.domain.charts.HeatmapChart
import ai.logsight.backend.charts.domain.charts.PieChart
import ai.logsight.backend.charts.domain.query.GetChartDataQuery

interface ChartsService {
    fun createHeatMap(getChartDataQuery: GetChartDataQuery): HeatmapChart
    fun createBarChart(getChartDataQuery: GetChartDataQuery): BarChart
    fun createPieChart(getChartDataQuery: GetChartDataQuery): PieChart
}
