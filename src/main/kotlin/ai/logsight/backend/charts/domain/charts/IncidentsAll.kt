package ai.logsight.backend.charts.domain.charts
import ai.logsight.backend.charts.repository.entities.elasticsearch.IncidentSourceAllOut

data class IncidentsAll(
    val incidentId: String,
    val source: IncidentSourceAllOut
)
