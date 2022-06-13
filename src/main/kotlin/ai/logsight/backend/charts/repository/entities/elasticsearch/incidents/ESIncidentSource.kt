package ai.logsight.backend.charts.repository.entities.elasticsearch.incidents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESIncidentSource(
    val timestamp: String,
    val risk: Long,
    @JsonProperty("count_messages")
    val countMessages: Long,
    @JsonProperty("count_states")
    val countStates: Long,
    val status: Long,
    @JsonProperty("added_states")
    val countAddedState: Long,
    @JsonProperty("level_faults")
    val countLevelFault: Long,
    val severity: Long,
    val tags: Map<String, String>,
    val countSemanticAnomaly: Long,
    val data: List<IncidentMessage>
)
