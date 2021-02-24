package com.loxbear.logsight.incidents.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.incidents.elasticsearch.Aggregations

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentsTable(val aggregations: Aggregations)