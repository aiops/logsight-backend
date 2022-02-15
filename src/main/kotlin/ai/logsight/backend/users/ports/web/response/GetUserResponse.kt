package ai.logsight.backend.users.ports.web.response

import java.util.*

data class GetUserResponse(
    val id: UUID,
    val email: String
)
