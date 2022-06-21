package ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESIncidentMessage(
    val timestamp: String,
    val template: String,
    val level: String,
    @JsonProperty("risk_score")
    val riskScore: Double,
    val message: String,
    val tags: Map<String, String>,
    @JsonProperty("added_state")
    val addedState: Long,
    val prediction: Long,
    @JsonProperty("risk_severity")
    val riskSeverity: Long,
    @JsonProperty("tag_string")
    val tagString: String
)
