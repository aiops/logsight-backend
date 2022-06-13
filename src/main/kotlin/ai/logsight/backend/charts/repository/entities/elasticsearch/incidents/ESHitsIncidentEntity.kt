package ai.logsight.backend.charts.repository.entities.elasticsearch.incidents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESHitsIncidentEntity(
    @JsonProperty("_id")
    val incidentId: String,
    @JsonProperty("_source")
    val source: ESIncidentSource
)
