package ai.logsight.backend.application.ports.web.requests

import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class DeleteApplicationRequest(
    @get:NotEmpty(message = "applicationId must not be empty")
    @get:Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "applicationId must be UUID type."
    )
    val applicationId: UUID
)
