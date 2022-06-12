package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentSourceAllOut(
    val timestamp: String,
    val risk: Long,
    val countMessages: Long,
    val countStates: Long,
    val status: Long,
    val countAddedState: Long,
    val countLevelFault: Long,
    val severity: Long,
    val tags: Map<String, String>,
    val countSemanticAnomaly: Long,
    val message: IncidentMessageOut,
)
