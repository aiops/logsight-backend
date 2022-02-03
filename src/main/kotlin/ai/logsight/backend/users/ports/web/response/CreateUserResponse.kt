package ai.logsight.backend.users.ports.web.response

import java.util.*

data class CreateUserResponse(
    val id: UUID,
    val email: String
)
