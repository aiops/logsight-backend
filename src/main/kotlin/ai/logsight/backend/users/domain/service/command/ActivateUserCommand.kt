package ai.logsight.backend.users.domain.service.command

import java.util.*

data class ActivateUserCommand(
    val id: UUID,
    val activationToken: UUID
)
