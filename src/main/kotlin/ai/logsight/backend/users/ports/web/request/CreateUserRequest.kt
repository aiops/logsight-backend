package ai.logsight.backend.users.ports.web.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class CreateUserRequest(
    @get:NotEmpty(message = "email must not be empty.")
    @get:Email(message = "email format must be valid (e.g., user@company.com).")
    val email: String,

    @get:NotEmpty(message = "password must not be empty.")
    @get:Size(min = 8, message = "password must be at least 8 characters.")
    val password: String,

)
