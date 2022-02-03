package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class ChangePasswordRequest(
    @NotEmpty(message = "email must not be empty.")
    @get:Email(message = "email format must be valid (e.g., user@company.com).")
    val email: String,
    @NotEmpty(message = "newPassword must not be empty.")
    @get:Size(min = 8, message = "newPassword must be at least 8 characters.")
    val newPassword: String,
    @NotEmpty(message = "confirmNewPassword must not be empty.")
    @get:Size(min = 8, message = "confirmNewPassword must be at least 8 characters.")
    val confirmNewPassword: String
) {
    @AssertTrue(message = "newPassword and confirmNewPassword must be equal")
    fun isValidPass(): Boolean {
        return Objects.equals(this.newPassword, this.confirmNewPassword)
    }
}
