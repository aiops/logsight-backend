package ai.logsight.backend.compare.controller.response

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class IncidentResponse(
    val startTimestamp: String,
    val stopTimestamp: String,
    val semanticThreats: JsonNode,
    val totalScore: Int // incident severities
)
