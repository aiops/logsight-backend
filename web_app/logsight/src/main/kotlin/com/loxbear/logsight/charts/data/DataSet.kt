package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataSet(
    val data: List<Double>,
    val label: String,
    val backgroundColor: String? = null,
    val borderColor: String? = null
)