package ai.logsight.backend.logs.ingestion.ports.web.requests

import ai.logsight.backend.logs.domain.LogEvent
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SendLogListRequest(
    @get:NotNull(message = "applicationId must be defined")
    val applicationId: UUID,
    @get:NotEmpty(message = "tag must not be empty")
    val tags: Map<String, String> = mapOf("default" to "default"),
    @get:Valid
    val logs: List<LogEvent> = listOf()
)
