package ai.logsight.backend.application.ports.web.responses

import java.util.*

data class CreateApplicationResponse(
    val description: String,
    val applicationName: String,
    val applicationId: UUID
)
