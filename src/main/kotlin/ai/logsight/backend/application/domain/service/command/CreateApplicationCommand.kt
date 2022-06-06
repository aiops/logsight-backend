package ai.logsight.backend.application.domain.service.command

import ai.logsight.backend.users.domain.User

data class CreateApplicationCommand(
    val applicationName: String,
    val user: User,
    val displayName: String,
    val elasticsearchIndex: String = "",
)
