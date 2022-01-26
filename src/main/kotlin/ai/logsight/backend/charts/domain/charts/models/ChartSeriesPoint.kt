package ai.logsight.backend.charts.domain.charts.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChartSeriesPoint(
    val name: String,
    val value: Double,
)
