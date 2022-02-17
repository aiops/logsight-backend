package ai.logsight.backend.users.ports.web.response

import java.util.*

data class GetUserResponse(
    val userId: UUID,
    val email: String
)
