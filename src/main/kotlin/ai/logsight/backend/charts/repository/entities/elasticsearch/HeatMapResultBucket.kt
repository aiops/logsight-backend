package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapResultBucket(
    val key: String,
    @JsonProperty("doc_count")
    val docCount: Double,
    @JsonProperty("1")
    val valueData: ValueResultBucket
)
