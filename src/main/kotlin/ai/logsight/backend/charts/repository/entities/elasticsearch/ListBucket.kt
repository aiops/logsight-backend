package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListBucket(
    val buckets: List<ValueResultBucket>
)