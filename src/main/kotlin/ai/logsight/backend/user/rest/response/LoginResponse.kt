package ai.logsight.backend.user.rest.response

import ai.logsight.backend.user.domain.User

data class LoginResponse(
    val token: String,
    val user: UserDTO
)
