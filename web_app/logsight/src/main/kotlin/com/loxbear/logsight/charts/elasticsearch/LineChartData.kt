package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.Aggregations

@JsonIgnoreProperties(ignoreUnknown = true)
data class LineChartData(val aggregations: Aggregations)