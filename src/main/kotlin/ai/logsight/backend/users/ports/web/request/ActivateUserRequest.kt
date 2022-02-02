package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.Email

class ActivateUserRequest(
    val id: UUID,
    @get:Email
    val email: String,
    val activationToken: UUID
)
