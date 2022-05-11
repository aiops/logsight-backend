package ai.logsight.backend.application.domain.service.command

import ai.logsight.backend.users.domain.User
import java.util.*

class DeleteApplicationCommand(
    val applicationId: UUID,
    val user: User
)
