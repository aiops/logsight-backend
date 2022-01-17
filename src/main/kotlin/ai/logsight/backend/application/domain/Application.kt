package ai.logsight.backend.application.domain

import ai.logsight.backend.application.adapters.persistence.ApplicationStatus
import ai.logsight.backend.connectors.Connector
import ai.logsight.backend.user.domain.User
import java.util.*

data class Application(
    val id: UUID,
    val name: String,
    val status: ApplicationStatus,
    val user: User
)
