package ai.logsight.backend.charts.domain.charts.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChartSeriesPoint(
    val name: String,
    val value: Double,
    val applicationId: UUID? = null
)
