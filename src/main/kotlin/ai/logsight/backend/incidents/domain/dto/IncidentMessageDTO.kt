package ai.logsight.backend.incidents.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentMessageDTO(
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[\\d+-:]+)",
        message = "startTime must be defined as ISO 8601 timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val timestamp: String,
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val template: String,
    @get:Pattern(
        regexp = "^INFO$|^WARNING$|^WARN$|^FINER$|^FINE$|^DEBUG$|^ERROR$|^ERR$|^EXCEPTION$|^SEVERE$",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE"
    )
    @JsonView(IncidentDTOViews.Reduced::class)
    val level: String,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val riskScore: Double,
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val message: String,
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val tags: Map<String, String>,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val addedState: Long,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val prediction: Long,
    @PositiveOrZero
    @JsonView(IncidentDTOViews.Reduced::class)
    val riskSeverity: Long,
    @get:NotEmpty(message = "startTime must not be empty.")
    @JsonView(IncidentDTOViews.Reduced::class)
    val tagString: String,
)
