package ai.logsight.backend.application.ports.web.requests

import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class DeleteApplicationRequest(
    @get:NotNull(message = "applicationId must not be empty")
    val applicationId: UUID
)
