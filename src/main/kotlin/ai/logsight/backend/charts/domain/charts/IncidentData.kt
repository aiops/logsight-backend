package ai.logsight.backend.charts.domain.charts
import ai.logsight.backend.charts.repository.entities.elasticsearch.IncidentSourceDataOut

data class IncidentData(
    val incidentId: String,
    val source: IncidentSourceDataOut
)
