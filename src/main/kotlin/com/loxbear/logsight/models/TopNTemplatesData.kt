package com.loxbear.logsight.models

data class TopNTemplatesData(
    val template: String,
    val count: Double,
    val percentage: Double? = null,
)