package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogQualityOverview(
    
    val key: String,
    val docCount: Double,
    val linguisticPrediction: Double,
    val logLevelScore: Double
)