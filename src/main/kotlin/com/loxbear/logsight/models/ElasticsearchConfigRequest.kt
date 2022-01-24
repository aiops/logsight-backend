package com.loxbear.logsight.models

data class ElasticsearchConfigRequest(
    val elasticsearchUrl: String,
    val elasticsearchIndex: String,
    val elasticsearchPeriod: String,
    val elasticsearchUser: String,
    val elasticsearchPassword: String
)
