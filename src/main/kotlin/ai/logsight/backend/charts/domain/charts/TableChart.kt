package ai.logsight.backend.charts.domain.charts
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsDataPoint
import com.loxbear.logsight.charts.data.IncidentRow

data class TableChart(
    val data: List<IncidentRow>
) : Chart
