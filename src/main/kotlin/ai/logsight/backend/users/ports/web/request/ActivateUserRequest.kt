package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

class ActivateUserRequest(
    @NotEmpty(message = "id must not be empty.")
    @Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "id must be UUID type."
    )
    val id: UUID,
    @NotEmpty(message = "activationToken must not be empty.")
    @Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "activationToken must be UUID type."
    )
    val activationToken: UUID
)
