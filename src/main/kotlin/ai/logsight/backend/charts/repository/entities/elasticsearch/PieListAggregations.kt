package com.loxbear.logsight.charts.elasticsearch

import ai.logsight.backend.charts.repository.entities.elasticsearch.ValueResultBucket
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PieListAggregations(val buckets: List<ValueResultBucket>)