package ai.logsight.backend.application.ports.web.requests

import java.util.*
import javax.validation.constraints.NotNull

data class DeleteApplicationRequest(
    @field:NotNull(message = "applicationId must not be empty")
    val applicationId: UUID
)
