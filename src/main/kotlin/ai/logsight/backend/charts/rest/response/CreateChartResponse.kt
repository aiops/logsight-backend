package ai.logsight.backend.charts.rest.response

import ai.logsight.backend.charts.domain.charts.Chart

data class CreateChartResponse(
    val data: Chart
)
