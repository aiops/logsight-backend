package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class HitsIncidentAllDataPoint(
    @JsonProperty("_id")
    val incidentId: String,
    @JsonProperty("_source")
    val source: IncidentSourceAll
)
