package ai.logsight.backend.charts.domain.service

import ai.logsight.backend.charts.domain.charts.BarChart
import ai.logsight.backend.charts.domain.charts.HeatmapChart
import ai.logsight.backend.charts.domain.charts.PieChart
import ai.logsight.backend.charts.domain.charts.TableChart
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import java.util.*

interface ChartsService {
    fun createBarChart(getChartDataQuery: GetChartDataQuery): BarChart
    fun createPieChart(getChartDataQuery: GetChartDataQuery): PieChart
    fun createTableChart(getChartDataQuery: GetChartDataQuery): TableChart
    fun getChartQuery(userId: UUID, createChartRequest: ChartRequest): GetChartDataQuery
    fun getAnalyticsIssuesKPI(userId: UUID, baselineTags: Map<String, String>): Map<Long, Long>
}
