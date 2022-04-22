package ai.logsight.backend.application.ports.web.requests

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class CreateApplicationRequest(
    @get:NotEmpty(message = "applicationName must not be empty string or null.")
    @get:Pattern(regexp = "^[a-zA-Z0-9][ a-zA-Z0-9_.-]+\$", message = "applicationName must follow the following regex pattern ^[a-zA-Z0-9][ a-zA-Z0-9_.-]+\\\$.")
    val applicationName: String
)
