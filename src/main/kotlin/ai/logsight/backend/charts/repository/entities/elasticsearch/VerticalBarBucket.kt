package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class VerticalBarBucket(
    @JsonProperty("key")
    val status: Long,
    @JsonProperty("doc_count")
    val count: Long
)
