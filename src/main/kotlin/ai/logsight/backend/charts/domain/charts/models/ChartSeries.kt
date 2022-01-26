package ai.logsight.backend.charts.domain.charts.models
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChartSeries(
    val name: String,
    val series: MutableList<ChartSeriesPoint>
)
