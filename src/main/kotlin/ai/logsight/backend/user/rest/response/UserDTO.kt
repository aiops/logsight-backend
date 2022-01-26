package ai.logsight.backend.user.rest.response

import ai.logsight.backend.user.domain.User
import java.util.*

data class UserDTO(
    val id: UUID,
    val email: String
)