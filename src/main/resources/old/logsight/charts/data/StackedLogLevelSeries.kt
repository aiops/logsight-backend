package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StackedLogLevelSeries (
        val name: String,
        val series: MutableList<StackedLogLevelPoint>
)
