package ai.logsight.backend.users.ports.web.response

import java.util.*

data class ActivateUserResponse(
    val id: UUID,
    val email: String
)
