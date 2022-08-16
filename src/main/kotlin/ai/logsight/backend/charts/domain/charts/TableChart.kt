package ai.logsight.backend.charts.domain.charts

import com.loxbear.logsight.charts.data.IncidentRow

data class TableChart(
    val data: List<IncidentRow>
) : Chart
