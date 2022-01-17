package ai.logsight.backend.application.service.command

import ai.logsight.backend.connectors.Connector
import ai.logsight.backend.user.domain.User

data class CreateApplicationCommand(
    val applicationName: String,
    val user: User
)
