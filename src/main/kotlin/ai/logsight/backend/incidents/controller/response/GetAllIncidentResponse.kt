package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.charts.domain.charts.IncidentsAll

data class GetAllIncidentResponse(
    val listIncident: List<IncidentsAll>
)
