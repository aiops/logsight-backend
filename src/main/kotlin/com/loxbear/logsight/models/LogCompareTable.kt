package com.loxbear.logsight.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHitCompare
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHitCompareCount
import org.json.JSONArray
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogCompareTable(
    val applicationId: Long,
    val indexName: String,
    val timestamp: String,
    val prediction: Double,
    val timestampStart: String,
    val timestampEnd: String,
    val compareTag: String,
    val baselineTag: String,
    val countAnomalyList: MutableList<VariableAnalysisHitCompareCount>,
    val newTemplateList: MutableList<VariableAnalysisHitCompare>,
    val ratioScore: Double,
    val numberOfNewAnomalies: Long,
    val numberOfNewNormal: Long
)