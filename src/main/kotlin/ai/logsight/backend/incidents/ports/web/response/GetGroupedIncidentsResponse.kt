package ai.logsight.backend.incidents.ports.web.response

import ai.logsight.backend.incidents.domain.dto.IncidentDTOViews
import ai.logsight.backend.incidents.domain.dto.IncidentGroupDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetGroupedIncidentsResponse(
    @JsonView(IncidentDTOViews.Reduced::class)
    val incidentGroups: List<IncidentGroupDTO>
)
