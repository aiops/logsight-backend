package com.loxbear.logsight.charts.elasticsearch

data class VariableAnalysisHit(
    val message: String,
    val template: String,
    val params: List<HitParam>,
    val timestamp: String,
    val actualLevel: String,
    val applicationId: Long
)
