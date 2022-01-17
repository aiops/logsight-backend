package ai.logsight.backend.application.ports.web.requests

import java.util.*

data class CreateApplicationRequest(
    val id: UUID,
    val name: String,
    val userId: UUID
)
