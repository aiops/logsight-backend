package ai.logsight.backend.logs.ports.web.requests

import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class SendLogListRequest(
    @get:NotNull
    val applicationId: UUID,
    val tag: String,
    @Pattern(regexp = "unknown")
    val logFormat: String,
    val logs: List<String>
)
