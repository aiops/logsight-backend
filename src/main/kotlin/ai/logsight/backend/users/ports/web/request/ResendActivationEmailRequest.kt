package ai.logsight.backend.users.ports.web.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class ResendActivationEmailRequest(
    @NotEmpty(message = "email must not be empty.")
    @get:Email(message = "email format must be valid (e.g., user@company.com).")
    val email: String
)
