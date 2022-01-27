package ai.logsight.backend.users.ports.web.request

import javax.validation.constraints.Email

class ForgotPasswordRequest(
    @get:Email
    val email: String
)
