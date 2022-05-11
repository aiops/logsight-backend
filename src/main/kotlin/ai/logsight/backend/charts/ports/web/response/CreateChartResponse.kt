package ai.logsight.backend.charts.ports.web.response

import ai.logsight.backend.charts.domain.charts.Chart

data class CreateChartResponse(
    val data: Chart
)
