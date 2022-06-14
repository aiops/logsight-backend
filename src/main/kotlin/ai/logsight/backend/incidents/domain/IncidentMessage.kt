package ai.logsight.backend.incidents.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentMessage(
    val timestamp: String,
    val template: String,
    val level: String,
    val riskScore: Double,
    val message: String,
    val tags: Map<String, String>,
    val addedState: Long,
    val prediction: Long,
    val riskSeverity: Long,
    val tagString: String,
)
