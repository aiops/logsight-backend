package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapBucket(
    @JsonProperty("key_as_string")
    val date: ZonedDateTime,
    val listBuckets: HeatMapListBucket
)
