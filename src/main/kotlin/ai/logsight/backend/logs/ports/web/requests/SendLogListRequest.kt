package ai.logsight.backend.logs.ports.web.requests

import ai.logsight.backend.logs.domain.LogFormat
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class SendLogListRequest(
    @get:NotEmpty
    val applicationId: UUID,
    @get:NotNull
    @get:NotEmpty
    val tag: String = "default",
    val logFormat: LogFormat = LogFormat.UNKNOWN_FORMAT,
    val logs: List<String>
)
