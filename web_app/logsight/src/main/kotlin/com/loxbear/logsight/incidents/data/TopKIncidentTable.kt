package com.loxbear.logsight.incidents.data

import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentTable(
        val indexName: String,
        val startTimestamp: String,
        val stopTimestamp: String,
        val firstLog: String,
        val lastLog: String,
        val totalScore: BigDecimal
)