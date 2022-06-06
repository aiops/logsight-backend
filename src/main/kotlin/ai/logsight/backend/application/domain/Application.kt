package ai.logsight.backend.application.domain

import ai.logsight.backend.users.domain.User
import java.util.*

data class Application(
    val id: UUID,
    val name: String,
    val displayName: String?,
    val status: ApplicationStatus,
    val user: User,
    val applicationKey: String,
    val index: String
)
