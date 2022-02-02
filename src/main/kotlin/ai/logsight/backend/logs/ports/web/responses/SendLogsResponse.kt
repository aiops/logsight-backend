package ai.logsight.backend.logs.ports.web.responses

import java.util.*

data class SendLogsResponse(
    val description: String,
    val applicationId: UUID,
    val tag: String
)
