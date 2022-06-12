package ai.logsight.backend.incidents.controller.response

import ai.logsight.backend.charts.domain.charts.IncidentData

data class GetIncidentByIdResponse(
    val incidentData: IncidentData
)
