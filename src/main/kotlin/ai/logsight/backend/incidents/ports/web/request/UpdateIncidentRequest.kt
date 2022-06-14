package ai.logsight.backend.incidents.ports.web.request

import ai.logsight.backend.incidents.domain.dto.IncidentDTO
import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class UpdateIncidentRequest(
    val incident: IncidentDTO
)
