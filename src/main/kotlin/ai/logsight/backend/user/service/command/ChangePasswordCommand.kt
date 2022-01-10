package ai.logsight.backend.user.service.command

import javax.validation.constraints.Email
import javax.validation.constraints.Size
import kotlin.String

data class ChangePasswordCommand(
    @get:Email
    val email: String,

    @get:Size(min = 8)
    val newPassword: String,

    @get:Size(min = 8)
    val confirmNewPassword: String
)
