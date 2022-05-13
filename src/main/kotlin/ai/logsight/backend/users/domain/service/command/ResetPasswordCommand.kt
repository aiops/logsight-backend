package ai.logsight.backend.users.domain.service.command

import java.util.*
import javax.validation.constraints.Size

class ResetPasswordCommand(
    val id: UUID,
    @get:Size(min = 8)
    val password: String,
    val passwordResetToken: UUID
)
