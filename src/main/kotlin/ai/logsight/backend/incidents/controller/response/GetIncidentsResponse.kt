package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.incidents.domain.Incident

data class GetIncidentsResponse(
    val incidents: List<Incident>
)
