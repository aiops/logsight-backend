package ai.logsight.backend.incidents.ports.web.request

import ai.logsight.backend.incidents.domain.dto.IncidentDTO

data class UpdateIncidentRequest(
    val incident: IncidentDTO
)