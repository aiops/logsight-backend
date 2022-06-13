package ai.logsight.backend.incidents.domain

import ai.logsight.backend.charts.repository.entities.elasticsearch.incidents.IncidentMessage
import com.fasterxml.jackson.annotation.JsonView

data class Incident(
    @JsonView(IncidentViews.Reduced::class)
    val incidentId: String,
    @JsonView(IncidentViews.Reduced::class)
    val timestamp: String,
    @JsonView(IncidentViews.Reduced::class)
    val risk: Long,
    @JsonView(IncidentViews.Reduced::class)
    val countMessages: Long,
    @JsonView(IncidentViews.Reduced::class)
    val countStates: Long,
    @JsonView(IncidentViews.Reduced::class)
    val status: Long,
    @JsonView(IncidentViews.Reduced::class)
    val countAddedState: Long,
    @JsonView(IncidentViews.Reduced::class)
    val countLevelFault: Long,
    @JsonView(IncidentViews.Reduced::class)
    val severity: Long,
    @JsonView(IncidentViews.Reduced::class)
    val tags: Map<String, String>,
    @JsonView(IncidentViews.Reduced::class)
    val countSemanticAnomaly: Long,
    @JsonView(IncidentViews.Reduced::class)
    val message: IncidentMessage,
    @JsonView(IncidentViews.Complete::class)
    val data: List<IncidentMessage>
)
