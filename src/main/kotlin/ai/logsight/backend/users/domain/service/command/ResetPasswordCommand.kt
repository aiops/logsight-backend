package ai.logsight.backend.users.domain.service.command

import java.util.*

class ResetPasswordCommand(
    val email: String,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
