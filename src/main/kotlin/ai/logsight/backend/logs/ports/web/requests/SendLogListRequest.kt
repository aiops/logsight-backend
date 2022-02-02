package ai.logsight.backend.logs.ports.web.requests

import java.util.*
import javax.validation.constraints.Pattern

data class SendLogListRequest(
    val appId: UUID,
    val tag: String,
    @Pattern(regexp = "unknown|json")
    val logFormat: String,
    val logs: List<String>
)
