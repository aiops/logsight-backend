package com.loxbear.logsight.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogQualityTable(
    val applicationId: Long,
    val indexName: String,
    val timestamp: String,
    val template: String,
    val appName: String,
    val message: String,
    val predictedLevel: String,
    val actualLevel: String,
    val linguisticPrediction: Integer,
    val hasSubject: Boolean,
    val hasObject: Boolean,
    val variableHit: VariableAnalysisHit
)