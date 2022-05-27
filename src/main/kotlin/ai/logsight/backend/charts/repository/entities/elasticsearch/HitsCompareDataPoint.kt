package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class HitsCompareDataPoint(
    @JsonProperty("_id")
    val compareId: String,
    @JsonProperty("_source")
    val source: JsonNode
)
