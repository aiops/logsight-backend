package com.loxbear.logsight.models

import kotlinx.serialization.*

@Serializable
data class LogMessage(
    val timestamp: String? = null,
    val message: String,
    val level: String,
)
