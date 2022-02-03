package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.*

class ResetPasswordRequest(
    @NotEmpty(message = "email must not be empty.")
    @get:Email(message = "email format must be valid (e.g., user@company.com).")
    val email: String,
    @NotEmpty(message = "password must not be empty.")
    @get:Size(min = 8, message = "password must be at least 8 characters.")
    val password: String,
    @NotEmpty(message = "repeatPassword must not be empty.")
    @get:Size(min = 8, message = "repeatPassword must be at least 8 characters.")
    val repeatPassword: String,
    val passwordResetToken: UUID
){
    @AssertTrue(message = "newPassword and confirmNewPassword must be equal")
    fun isValidPass(): Boolean {
        return Objects.equals(this.password, this.repeatPassword)
    }
}
