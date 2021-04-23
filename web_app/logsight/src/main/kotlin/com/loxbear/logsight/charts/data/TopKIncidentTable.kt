package com.loxbear.logsight.charts.data

import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentTable(
        val indexName: String,
        val startTimestamp: String,
        val stopTimestamp: String,
        val newTemplates: String,
        val semanticAD: String,
        val countAD: String,
        val totalScore: BigDecimal // incident severities
)