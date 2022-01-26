package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogCountLineChart(
    @JsonProperty("key_as_string")
    val date: ZonedDateTime,
    @JsonProperty("doc_count")
    val docCount: Double,
)