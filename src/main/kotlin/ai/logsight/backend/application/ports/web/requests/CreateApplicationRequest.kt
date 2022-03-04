package ai.logsight.backend.application.ports.web.requests

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class CreateApplicationRequest(
    @get:NotEmpty(message = "applicationName must not be empty string or null.")
    @get:Pattern(regexp = "^[a-z0-9_]*$", message = "applicationName must contain only lowercase letters, numbers ([a-z0-9_]), an underscore is allowed.")
    val applicationName: String
)
