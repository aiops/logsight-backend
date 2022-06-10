package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsIncidentDataPoint

data class GetIncidentByIdResponse(
    val listIncident: List<HitsIncidentDataPoint>
)
