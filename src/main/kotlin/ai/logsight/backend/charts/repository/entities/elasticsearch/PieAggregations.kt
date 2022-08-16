package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PieAggregations(
    val error: ValueResultBucket,
    val info: ValueResultBucket,
    val debug: ValueResultBucket,
    val warn: ValueResultBucket
)
