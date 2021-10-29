package com.loxbear.logsight.models.log

import kotlinx.serialization.Serializable

@Serializable
data class LogMessageKafka (
    val privateKey: String,
    val appName: String,
    val message: String,
)