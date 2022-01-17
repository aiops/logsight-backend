package ai.logsight.backend.user.adapters.rest.request

import java.util.UUID
import javax.validation.constraints.Email

class ResetPasswordRequest(
    @get:Email
    val email: String,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
