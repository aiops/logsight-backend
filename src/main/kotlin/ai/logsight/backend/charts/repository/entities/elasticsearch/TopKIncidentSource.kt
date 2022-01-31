package ai.logsight.backend.charts.repository.entities.elasticsearch
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentSource(
    @JsonProperty("@timestamp")
    val timestamp: String,
    @JsonProperty("timestamp_start")
    val startTimestamp: String,
    @JsonProperty("timestamp_end")
    val stopTimestamp: String,
    @JsonProperty("new_templates")
    val newTemplates: String,
    @JsonProperty("semantic_ad")
    val semanticAD: String,
    @JsonProperty("count_ads")
    val countAD: String,
    @JsonProperty("semantic_count_ads")
    val scAnomalies: String,
    @JsonProperty("logs")
    val logData: String,
    @JsonProperty("total_score")
    val totalScore: Int
)
