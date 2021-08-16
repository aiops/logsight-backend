package com.loxbear.logsight.models

import com.loxbear.logsight.entities.enums.DateTimeType

data class PredefinedTimeRequest(
    val name: String,
    val startTime: String,
    val endTime: String,
    val dateTimeType: DateTimeType,
)