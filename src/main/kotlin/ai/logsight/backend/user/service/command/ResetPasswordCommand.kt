package ai.logsight.backend.user.service.command

import java.util.UUID
import kotlin.String

class ResetPasswordCommand(
    val email: String,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
