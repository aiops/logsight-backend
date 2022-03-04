package ai.logsight.backend.compare.controller.response

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class IncidentResponse(
    val applicationId: UUID?,
    val startTime: String,
    val stopTime: String,
    val semanticThreats: JsonNode,
    val totalScore: Int // incident severities
)
