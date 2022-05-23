package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ResetPasswordRequest(
    @get:NotNull(message = "id must not be empty.")
    val userId: UUID,
    @get:NotEmpty(message = "password must not be empty.")
    @get:Size(min = 8, message = "password must be at least 8 characters.")
    val password: String,
    @get:NotNull(message = "passwordResetToken must not be empty.")
    val passwordResetToken: UUID
)
