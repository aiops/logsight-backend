package com.loxbear.logsight.incidents.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.incidents.elasticsearch.Bucket

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListAggregations(val buckets: List<Bucket>)