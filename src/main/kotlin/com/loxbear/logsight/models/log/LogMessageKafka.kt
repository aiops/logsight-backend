package com.loxbear.logsight.models.log

import kotlinx.serialization.Serializable

@Serializable
data class LogMessageKafka (
    val private_key: String,
    val app_name: String,
    val log_type: String,
    val message: String
)