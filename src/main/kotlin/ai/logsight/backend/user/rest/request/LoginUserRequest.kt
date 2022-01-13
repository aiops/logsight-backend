package ai.logsight.backend.user.rest.request

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class LoginUserRequest(
    @get:Email
    val email: String,

    @get:Size(min=8)
    val password: String
)