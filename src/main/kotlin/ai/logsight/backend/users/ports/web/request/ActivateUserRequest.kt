package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class ActivateUserRequest(
    @get:NotNull(message = "id must not be empty.")
    val id: UUID,
    @get:NotNull(message = "activationToken must not be empty.")
    val activationToken: UUID
)
