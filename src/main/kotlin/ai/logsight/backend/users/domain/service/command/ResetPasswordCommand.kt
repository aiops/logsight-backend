package ai.logsight.backend.users.domain.service.command

import java.util.*

class ResetPasswordCommand(
    val id: UUID,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
