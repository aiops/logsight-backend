package ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESHitsIncident(
    @JsonProperty("_id")
    val incidentId: String,
    @JsonProperty("_source")
    val source: ESIncident
)
