package ai.logsight.backend.user.ports.web.request

import java.util.UUID
import javax.validation.constraints.Email

class ResetPasswordRequest(
    @get:Email
    val email: String,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
