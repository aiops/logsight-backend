package com.loxbear.logsight.charts.elasticsearch

import java.time.LocalDateTime

data class VariableAnalysisSpecificTemplate(
    val timestamp: LocalDateTime,
    val param: String,
)