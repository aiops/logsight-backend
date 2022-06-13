package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.incidents.domain.Incident

data class GetAllIncidentResponse(
    val listIncident: List<Incident>
)
