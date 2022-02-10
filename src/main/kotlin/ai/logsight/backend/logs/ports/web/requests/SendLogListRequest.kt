package ai.logsight.backend.logs.ports.web.requests

import ai.logsight.backend.logs.domain.LogFormats
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SendLogListRequest(
    @get:NotNull(message = "applicationId must be defined")
    val applicationId: UUID,
    @get:NotEmpty(message = "tag must not be empty")
    val tag: String = "default",
    val logFormats: LogFormats = LogFormats.UNKNOWN_FORMAT,
    val logs: List<String> = listOf()
)
