package ai.logsight.backend.user.service.command

import java.util.UUID

class ResetPasswordCommand(
    val userId: UUID,
    val password: String,
    val passwordResetToken: UUID
)
