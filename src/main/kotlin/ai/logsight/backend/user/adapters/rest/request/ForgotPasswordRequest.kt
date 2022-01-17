package ai.logsight.backend.user.adapters.rest.request

import javax.validation.constraints.Email

class ForgotPasswordRequest(
    @get:Email
    val email: String
)
