package ai.logsight.backend.users.domain.service.command

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class ChangePasswordCommand(
    @get:Email
    val email: String,

    @get:Size(min = 8)
    val oldPassword: String,

    @get:Size(min = 8)
    val newPassword: String,
)
