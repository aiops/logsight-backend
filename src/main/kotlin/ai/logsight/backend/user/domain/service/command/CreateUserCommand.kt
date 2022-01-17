package ai.logsight.backend.user.domain.service.command

import kotlin.String

data class CreateUserCommand(
    val email: String,
    val password: String,
)

