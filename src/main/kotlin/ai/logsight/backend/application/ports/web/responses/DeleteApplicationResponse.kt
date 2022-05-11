package ai.logsight.backend.application.ports.web.responses

import java.util.*

data class DeleteApplicationResponse(
    val applicationName: String,
    val applicationId: UUID
)
