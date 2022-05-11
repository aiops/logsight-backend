package ai.logsight.backend.charts.repository.entities.elasticsearch
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class BarBucket(
    @JsonProperty("key_as_string")
    val date: ZonedDateTime,
    val bucketInfo: ValueResultBucket = ValueResultBucket(0.0),
    val bucketWarning: ValueResultBucket = ValueResultBucket(0.0),
    val bucketError: ValueResultBucket = ValueResultBucket(0.0),
    val bucketPrediction: ValueResultBucket = ValueResultBucket(0.0),
)
