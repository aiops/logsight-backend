package ai.logsight.backend.users.domain.service.command

import java.util.*

data class ActivateUserCommand(
    val email: String,
    val activationToken: UUID
)
