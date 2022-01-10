package ai.logsight.backend.user.service.command

data class ChangePasswordCommand(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)
