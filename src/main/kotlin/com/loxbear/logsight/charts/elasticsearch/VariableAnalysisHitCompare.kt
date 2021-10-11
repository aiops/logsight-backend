package com.loxbear.logsight.charts.elasticsearch

data class VariableAnalysisHitCompare(
    val message: String,
    val template: String,
    val prediction: String,
    val params: List<HitParam>,
    val timestamp: String,
    val actualLevel: String,
    val applicationId: Long,
    val expectedRate: Double,
    val rateNow: Double
)
