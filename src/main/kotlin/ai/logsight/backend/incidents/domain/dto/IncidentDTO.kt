package ai.logsight.backend.incidents.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import javax.validation.constraints.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentDTO(
    @get:NotNull(message = "id must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val incidentId: String,
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[\\d+-:]+)",
        message = "startTime must be defined as ISO 8601 timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val timestamp: String,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val risk: Long,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val countMessages: Long,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val countStates: Long,
    @get:Min(1)
    @get:Max(3)
    @JsonView(IncidentDTOViews.Reduced::class)
    val status: Long,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val countAddedState: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val countLevelFault: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val severity: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    val tags: Map<String, String>,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val countSemanticAnomaly: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    val message: IncidentMessageDTO,
    @JsonView(IncidentDTOViews.Complete::class)
    val data: List<IncidentMessageDTO>? = null
)
