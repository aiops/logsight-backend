package ai.logsight.backend.user.ports.web.request

import java.util.UUID
import javax.validation.constraints.Email

class ActivateUserRequest(
    val id: UUID,
    @get:Email
    val email: String,
    val activationToken: UUID
)
