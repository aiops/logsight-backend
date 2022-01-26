package ai.logsight.backend.charts

import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import com.loxbear.logsight.charts.data.HeatmapChart

interface ChartsService {
    fun createHeatmap(getChartDataQuery: GetChartDataQuery): HeatmapChart
}
