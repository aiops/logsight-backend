package ai.logsight.backend.user.domain

import java.util.UUID

data class LocalUser(
    val id: UUID,
    val privateKey: String
)
