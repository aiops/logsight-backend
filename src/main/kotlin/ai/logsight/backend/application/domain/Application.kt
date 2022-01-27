package ai.logsight.backend.application.domain

import ai.logsight.backend.application.ports.out.persistence.ApplicationStatus
import ai.logsight.backend.users.domain.User
import java.util.*

data class Application(
    val id: UUID,
    val name: String,
    var status: ApplicationStatus,
    val user: User,
    val applicationKey: String
)
