package ai.logsight.backend.security.authentication.response

import java.util.*

data class UserDTO(
    val id: UUID,
    val email: String
)
