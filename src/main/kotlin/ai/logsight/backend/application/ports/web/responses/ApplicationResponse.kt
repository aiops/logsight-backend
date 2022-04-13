package ai.logsight.backend.application.ports.web.responses

import java.util.*

data class ApplicationResponse(
    val applicationId: UUID,
    val name: String
)
