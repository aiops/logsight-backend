package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.Email

class ResetPasswordRequest(
    @get:Email
    val email: String,
    val password: String,
    val repeatPassword: String,
    val passwordResetToken: UUID
)
