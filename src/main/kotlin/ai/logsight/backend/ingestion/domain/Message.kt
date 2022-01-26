package ai.logsight.backend.ingestion.domain

import kotlinx.serialization.Serializable

@Serializable
data class LogMessage(val message: String)
