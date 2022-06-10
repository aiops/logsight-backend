package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentSourcePoint(
    val tags: Map<String, String>,
    val timestamp: String,
    val risk: Long,
    val severity: Long,
    val status: Long,
    val message: String,
)
