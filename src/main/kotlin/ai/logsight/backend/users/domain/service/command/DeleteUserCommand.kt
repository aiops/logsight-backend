package ai.logsight.backend.users.domain.service.command

import java.util.*

data class DeleteUserCommand(
    val userId: UUID
)
