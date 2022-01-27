package ai.logsight.backend.users.domain

import java.util.UUID

data class LocalUser(
    val id: UUID,
    val elasticsearchKey: String,
)
