package ai.logsight.backend.incidents.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentGroupDTO(
    @JsonView(IncidentDTOViews.Reduced::class)
    val head: IncidentDTO,
    @JsonView(IncidentDTOViews.Reduced::class)
    val incidents: List<IncidentDTO>
)
