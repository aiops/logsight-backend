package ai.logsight.backend.incidents.ports.web.response

import ai.logsight.backend.incidents.domain.dto.IncidentDTO

data class UpdateIncidentResponse(
    val incidentDTO: IncidentDTO
)
