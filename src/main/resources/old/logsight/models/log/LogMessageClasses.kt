package com.loxbear.logsight.models.log

import kotlinx.serialization.Serializable

@Serializable
data class LogMessage(val message: String)