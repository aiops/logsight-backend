package ai.logsight.backend.users.ports.web.request

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class CreateUserRequest(
    @get:Email
    val email: String,

    @get:Size(min = 8)
    val password: String,

    @get:Size(min = 8)
    val repeatPassword: String
)
