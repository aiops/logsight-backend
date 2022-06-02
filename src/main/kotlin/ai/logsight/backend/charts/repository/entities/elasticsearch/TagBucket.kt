package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class TagBucket(
    @JsonProperty("key")
    val tagValue: String,
    @JsonProperty("doc_count")
    val tagCount: Long
)
