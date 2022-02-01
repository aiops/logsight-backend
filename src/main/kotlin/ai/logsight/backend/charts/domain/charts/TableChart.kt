package ai.logsight.backend.charts.domain.charts
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsDataPoint

data class TableChart(
    val data: List<HitsDataPoint>
) : Chart
