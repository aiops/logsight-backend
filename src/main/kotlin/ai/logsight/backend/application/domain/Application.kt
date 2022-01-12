package ai.logsight.backend.application.domain

import ai.logsight.backend.application.persistence.ApplicationStatus
import ai.logsight.backend.user.domain.User
import java.util.*

data class Application(
    val id: UUID,
    val name: String,
    val status: ApplicationStatus,
    val user: User

)
