package com.loxbear.logsight.models.log

import kotlinx.serialization.Serializable

/*
This must be done like this due to kotlin's serialization immaturity.

#### Be careful when changing the structure. Requires adaption in logstash. #####
 */

@Serializable
sealed class LogMessage {
    abstract val message: String
}

@Serializable
data class LogMessageBasic (
    override val message: String
) : LogMessage()

@Serializable
data class LogMessageLogsight (
    val timestamp: String? = null,
    val level: String? = null,
    override val message: String
) : LogMessage()