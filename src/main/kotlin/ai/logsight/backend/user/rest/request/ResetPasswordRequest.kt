package ai.logsight.backend.user.rest.request

import java.util.UUID

class ResetPasswordRequest(
    val userId: UUID,
    val password: String,
    val passwordResetToken: UUID
)
