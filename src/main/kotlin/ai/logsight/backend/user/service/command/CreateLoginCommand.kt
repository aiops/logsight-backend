package ai.logsight.backend.user.service.command

data class CreateLoginCommand(
    val email: String,
    val password: String
)
