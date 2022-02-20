package ai.logsight.backend.users.domain.service.command

data class CreateUserCommand(
    val email: String,
    val password: String,
)
