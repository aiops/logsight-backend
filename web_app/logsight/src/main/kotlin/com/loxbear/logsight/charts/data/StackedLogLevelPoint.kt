package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StackedLogLevelPoint(
        val name: String,
        val value: Double
)
