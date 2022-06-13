package ai.logsight.backend.charts.repository.entities.elasticsearch.incidents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentMessage(
    val timestamp: String,
    val template: String,
    val level: String,
    @param:JsonProperty("risk_score")
    val riskScore: Double,
    val message: String,
    val tags: Map<String, String>,
    @param:JsonProperty("added_state")
    val addedState: Long,
    val prediction: Long,
    @param:JsonProperty("risk_severity")
    val riskSeverity: Long,
    @param:JsonProperty("tag_string")
    val tagString: String,
)
