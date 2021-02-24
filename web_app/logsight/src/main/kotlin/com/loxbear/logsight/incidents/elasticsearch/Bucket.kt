package com.loxbear.logsight.incidents.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Bucket(
    @JsonProperty("key_as_string")
    val date: ZonedDateTime,
    val listBuckets: ListBucket
)