package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class ChangePasswordRequest(
    @get:NotEmpty(message = "oldPassword must not be empty.")
    @get:Size(min = 8, message = "oldPassword must be at least 8 characters.")
    val oldPassword: String,
    @get:NotEmpty(message = "newPassword must not be empty.")
    @get:Size(min = 8, message = "newPassword must be at least 8 characters.")
    val newPassword: String,
    @get:NotEmpty(message = "confirmNewPassword must not be empty.")
    @get:Size(min = 8, message = "confirmNewPassword must be at least 8 characters.")
    val repeatNewPassword: String
) {
    @AssertTrue(message = "newPassword and confirmNewPassword must be equal")
    fun isValidPass(): Boolean {
        return Objects.equals(this.newPassword, this.repeatNewPassword)
    }
}
