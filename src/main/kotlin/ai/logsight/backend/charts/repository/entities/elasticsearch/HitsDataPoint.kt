package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class HitsDataPoint(
    @JsonProperty("_index")
    val indexName: String,
    @JsonProperty("_source")
    val source: TopKIncidentSource
)
