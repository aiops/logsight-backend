package ai.logsight.backend.user.rest.request

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class ChangePasswordRequest(
    @get:Email
    val email: String,

    @get:Size(min = 8)
    val newPassword: String,

    @get:Size(min = 8)
    val confirmNewPassword: String
)
