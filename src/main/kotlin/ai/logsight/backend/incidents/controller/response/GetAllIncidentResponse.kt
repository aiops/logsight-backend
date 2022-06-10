package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsIncidentAllDataPoint

data class GetAllIncidentResponse(
    val listIncident: List<HitsIncidentAllDataPoint>
)
