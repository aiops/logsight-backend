package ai.logsight.backend.application.domain.service.command

import ai.logsight.backend.user.domain.User

data class CreateApplicationCommand(
    val applicationName: String,
    val user: User
)
