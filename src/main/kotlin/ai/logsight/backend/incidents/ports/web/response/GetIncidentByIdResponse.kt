package ai.logsight.backend.incidents.ports.web.response

import ai.logsight.backend.incidents.domain.dto.IncidentDTO
import ai.logsight.backend.incidents.domain.dto.IncidentDTOViews
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetIncidentByIdResponse(
    @JsonView(IncidentDTOViews.Reduced::class)
    val incident: IncidentDTO
)
