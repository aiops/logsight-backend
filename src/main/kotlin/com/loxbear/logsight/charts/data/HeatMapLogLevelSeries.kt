package com.loxbear.logsight.charts.data
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapLogLevelSeries (
        val name: String,
        val series: MutableList<HeatMapLogLevelPoint>
)
