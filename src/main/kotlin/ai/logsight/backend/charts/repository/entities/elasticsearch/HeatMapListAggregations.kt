package com.loxbear.logsight.charts.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.Bucket

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapListAggregations(val buckets: List<HeatMapBucket>)