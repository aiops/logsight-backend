package com.loxbear.logsight.charts.elasticsearch

data class VariableAnalysisHitCompareCount(
    val message: String,
    val template: String,
    val prediction: String,
    val params: List<HitParam>,
    val timestamp: String,
    val actualLevel: String,
    val applicationId: Long,
    val expectedRate: Double,
    val rateNow: Double,
    val tag: String,
    val templateToGoList: MutableList<VariableAnalysisHitCompare>,
    val ratioScore: Double
)
