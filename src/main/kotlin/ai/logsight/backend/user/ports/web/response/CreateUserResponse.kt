package ai.logsight.backend.user.ports.web.response

import java.util.UUID

data class CreateUserResponse(
    val id: UUID,
    val email: String,
    val privateKey: String
)
