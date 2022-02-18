package ai.logsight.backend.security.authentication.response

import java.util.*

data class GetUserResponse(
    val userId: UUID,
    val email: String
)
