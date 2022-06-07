package ai.logsight.backend.logs.ingestion.domain.dto

import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage

data class LogEventsDTO(
    val index: String,
    val logs: List<SendLogMessage>,
    val tags: Map<String, String> = mapOf("default" to "default"),

)
