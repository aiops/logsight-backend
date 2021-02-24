package com.loxbear.logsight.incidents.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListBucket(
    val buckets: List<ResultBucket>
)