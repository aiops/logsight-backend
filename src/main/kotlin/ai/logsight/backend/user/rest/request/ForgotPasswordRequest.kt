package ai.logsight.backend.user.rest.request

import javax.validation.constraints.Email

class ForgotPasswordRequest(
    @get:Email
    val email: String
)
