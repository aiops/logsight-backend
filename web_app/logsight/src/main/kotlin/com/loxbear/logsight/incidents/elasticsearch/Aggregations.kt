package com.loxbear.logsight.incidents.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Aggregations (val listAggregations: ListAggregations)