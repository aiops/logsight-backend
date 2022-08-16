package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentSource(
    @JsonProperty("timestamp")
    val timestamp: String,
    @JsonProperty("timestamp_start")
    val startTimestamp: String,
    @JsonProperty("timestamp_end")
    val stopTimestamp: String,
    @JsonProperty("application_id")
    val applicationId: String?,
    @JsonProperty("new_templates")
    @JsonRawValue
    val newTemplates: JsonNode,
    @JsonProperty("semantic_ad")
    @JsonRawValue
    val semanticAD: JsonNode,
    @JsonProperty("count_ads")
    @JsonRawValue
    val countAD: JsonNode,
    @JsonRawValue
    @JsonProperty("semantic_count_ads")
    val scAnomalies: JsonNode,
    @JsonProperty("logs")
    @JsonRawValue
    val logData: JsonNode,
    @JsonProperty("total_score")
    val totalScore: Int
)
