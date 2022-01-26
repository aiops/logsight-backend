package com.loxbear.logsight.charts.elasticsearch

import ai.logsight.backend.charts.repository.entities.elasticsearch.Aggregations
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LineChartData(val aggregations: Aggregations)
