package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class HitsCompareAllDataPoint(
    @JsonProperty("_id")
    val compareId: String,
    @JsonProperty("_source")
    val source: CompareSourcePoint
)
