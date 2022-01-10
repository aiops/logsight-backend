package ai.logsight.backend.user.service.command

import java.util.UUID

data class ActivateUserCommand(
    val id: UUID,
    val email: String,
    val activationToken: UUID
)
