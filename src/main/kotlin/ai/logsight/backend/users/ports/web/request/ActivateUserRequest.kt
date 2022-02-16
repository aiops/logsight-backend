package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class ActivateUserRequest(
    @get:NotNull(message = "id must not be empty.")
    @get:Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "id must be UUID type."
    )
    val id: String,
    @get:NotNull(message = "activationToken must not be empty.")
    val activationToken: UUID
)
