package ai.logsight.backend.security.authentication.response

import java.util.*

data class UserDTO(
    val userId: UUID,
    val email: String
)
