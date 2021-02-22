package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogLevelPoint (
    val name: String,
    val value: Double,
    val extra: PieExtra
    )
