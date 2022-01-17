package ai.logsight.backend.application.ports.web.requests

import java.util.UUID

data class DeleteApplicationRequest(
    val id: UUID
)
