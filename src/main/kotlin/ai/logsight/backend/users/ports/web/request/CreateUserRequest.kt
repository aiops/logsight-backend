package ai.logsight.backend.users.ports.web.request

import java.util.*
import javax.validation.constraints.*

data class CreateUserRequest(
    @get:NotEmpty(message = "email must not be empty.")
    @get:Email(message = "email format must be valid (e.g., user@company.com).")
    val email: String,

    @get:NotEmpty(message = "password must not be empty.")
    @get:Size(min = 8, message = "password must be at least 8 characters.")
    val password: String,

    @get:NotEmpty(message = "repeatPassword must not be empty.")
    @get:Size(min = 8, message = "repeatPassword must be at least 8 characters.")
    val repeatPassword: String,
) {
    @AssertTrue(message = "password and repeatPassword must be equal")
    fun isValidPass(): Boolean {
        return Objects.equals(this.password, this.repeatPassword)
    }
}
