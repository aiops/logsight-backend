package ai.logsight.backend.incidents.domain.dto

import ai.logsight.backend.incidents.domain.IncidentMessage
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncidentMessage
import com.fasterxml.jackson.annotation.JsonView
import javax.validation.constraints.*

data class IncidentDTO(
    @JsonView(IncidentDTOViews.Reduced::class)
    @get:NotNull(message = "id must not be empty.")
    @get:Pattern(
        regexp = "^[\\dA-Fa-f]{8}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{12}$",
        message = "id must be UUID type."
    )
    val incidentId: String,
    @JsonView(IncidentDTOViews.Reduced::class)
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[\\d+-:]+)",
        message = "startTime must be defined as ISO 8601 timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "startTime must not be empty.")
    val timestamp: String,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val risk: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val countMessages: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
    val countStates: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @get:Min(1)
    @get:Max(3)
    val status: Long,
    @JsonView(IncidentDTOViews.Reduced::class)
    @PositiveOrZero
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
    val message: IncidentMessage,
    @JsonView(IncidentDTOViews.Complete::class)
    val data: List<IncidentMessage>? = null
)
