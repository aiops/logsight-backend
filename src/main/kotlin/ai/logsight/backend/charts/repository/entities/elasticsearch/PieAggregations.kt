package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class PieAggregations(
    val error: ValueResultBucket,
    val info: ValueResultBucket,
    val debug: ValueResultBucket,
    val warn: ValueResultBucket
)