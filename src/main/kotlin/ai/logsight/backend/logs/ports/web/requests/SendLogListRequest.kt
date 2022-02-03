package ai.logsight.backend.logs.ports.web.requests

import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class SendLogListRequest(
    @get:NotNull
    val applicationId: UUID,
    @get:NotEmpty
    @get:NotNull
    val tag: String = "default",
    @Pattern(regexp = "unknown")
    val logFormat: String = "unknown",
    val logs: List<String>
)
