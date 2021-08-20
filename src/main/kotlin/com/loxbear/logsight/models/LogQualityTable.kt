package com.loxbear.logsight.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import org.json.JSONArray
import java.math.BigDecimal

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
    val linguisticPrediction: BigDecimal,
    val suggestions: MutableList<String>,
    val tags: String,
    val variableHit: VariableAnalysisHit
)