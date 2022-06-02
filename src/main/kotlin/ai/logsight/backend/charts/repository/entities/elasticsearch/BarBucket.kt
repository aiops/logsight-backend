package ai.logsight.backend.charts.repository.entities.elasticsearch
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class BarBucket(
    @JsonProperty("key_as_string")
    val date: ZonedDateTime,
    @JsonProperty("doc_count")
    val docCount: Double = 0.0,
    val bucketInfo: ValueResultBucket = ValueResultBucket(0.0),
    val bucketVelocity: ValueResultBucket = ValueResultBucket(0.0),
    val bucketMinVelocity: ValueResultBucket = ValueResultBucket(0.0),
    val bucketMaxVelocity: ValueResultBucket = ValueResultBucket(0.0),
    val bucketWarning: ValueResultBucket = ValueResultBucket(0.0),
    val bucketError: ValueResultBucket = ValueResultBucket(0.0),
    val bucketPrediction: ValueResultBucket = ValueResultBucket(0.0),
    val bucketMinRisk: ValueResultBucket = ValueResultBucket(0.0),
    val bucketMeanRisk: ValueResultBucket = ValueResultBucket(0.0),
    val bucketMaxRisk: ValueResultBucket = ValueResultBucket(0.0),
    val bucketFailureRatio: ValueResultBucket = ValueResultBucket(0.0)
)
