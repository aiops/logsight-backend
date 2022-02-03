package ai.logsight.backend.application.ports.web.requests

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class CreateApplicationRequest(
    @NotEmpty(message = "applicationName must not be empty string or null.")
    @Pattern(regexp = "^[a-z0-9_]*$", message = "applicationName must contain only lowercase letters, numbers, and an underscore ([a-z0-9_]).")
    val applicationName: String
)
