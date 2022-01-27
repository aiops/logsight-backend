package ai.logsight.backend.security.authentication.response

data class LoginResponse(
    val token: String,
    val user: UserDTO
)
