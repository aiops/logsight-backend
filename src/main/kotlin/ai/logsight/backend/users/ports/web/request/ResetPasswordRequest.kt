package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.*

class ResetPasswordRequest(
    @get:NotNull(message = "id must not be empty.")
    val id: UUID,
    @get:NotEmpty(message = "password must not be empty.")
    @get:Size(min = 8, message = "password must be at least 8 characters.")
    val password: String,
    @get:NotEmpty(message = "repeatPassword must not be empty.")
    @get:Size(min = 8, message = "repeatPassword must be at least 8 characters.")
    val repeatPassword: String,
    @get:NotNull(message = "passwordResetToken must not be empty.")
    val passwordResetToken: UUID
) {
    @AssertTrue(message = "newPassword and repeatPassword must be equal")
    fun isValidPass(): Boolean {
        return Objects.equals(this.password, this.repeatPassword)
    }
}
