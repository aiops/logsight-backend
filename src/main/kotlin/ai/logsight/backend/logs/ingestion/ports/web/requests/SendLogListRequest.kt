package ai.logsight.backend.logs.ingestion.ports.web.requests

import ai.logsight.backend.logs.domain.LogEvent
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class SendLogListRequest(
    @get:NotEmpty(message = "tag must not be empty")
    val tags: Map<String, String> = mapOf("default" to "default"),
    @get:Valid
    val logs: List<LogEvent> = listOf()
)
