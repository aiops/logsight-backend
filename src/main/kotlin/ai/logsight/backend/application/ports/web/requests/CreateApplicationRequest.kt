package ai.logsight.backend.application.ports.web.requests

import javax.validation.constraints.Pattern

data class CreateApplicationRequest(
    @Pattern(regexp = "^[a-z0-9_]*$")
    val applicationName: String
)
