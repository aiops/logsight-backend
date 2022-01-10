package ai.logsight.backend.user.service.command

data class CreateUserCommand(
    val email: String,
    val password: String,
)

