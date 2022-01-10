package ai.logsight.backend.user.service.command

import java.util.UUID

data class ActivateUserCommand(
    val email: String,
    val activationToken: UUID
)
