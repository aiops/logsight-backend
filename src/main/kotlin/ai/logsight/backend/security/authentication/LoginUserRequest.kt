package ai.logsight.backend.security.authentication

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class LoginUserRequest(
    @get:Email
    val email: String,

    @get:Size(min = 8)
    val password: String
)
