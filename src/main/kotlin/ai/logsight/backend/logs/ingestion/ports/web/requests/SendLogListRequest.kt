package ai.logsight.backend.logs.ingestion.ports.web.requests

import ai.logsight.backend.logs.domain.LogMessage
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SendLogListRequest(
    @get:NotNull(message = "applicationId must be defined")
    val applicationId: UUID,
    @get:NotEmpty(message = "tag must not be empty")
    val tag: String = "default",
    val logs: List<LogMessage> = listOf()
)
