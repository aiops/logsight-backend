package com.loxbear.logsight.charts.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopKIncidentTable(
    val applicationId: Long,
    val indexName: String,
    val timestamp: String,
    val startTimestamp: String,
    val stopTimestamp: String,
    val newTemplates: String,
    val semanticAD: String,
    val countAD: String,
    val scAnomalies: String,
    val totalScore: Int // incident severities
)