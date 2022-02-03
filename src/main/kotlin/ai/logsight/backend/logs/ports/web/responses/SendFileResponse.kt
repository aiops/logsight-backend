package ai.logsight.backend.logs.ports.web.responses

import java.util.*

data class SendFileResponse(
    val description: String,
    val applicationId: UUID
)
