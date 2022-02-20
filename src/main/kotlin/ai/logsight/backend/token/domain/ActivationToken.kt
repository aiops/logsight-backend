package ai.logsight.backend.token.domain

import java.time.LocalDateTime
import java.util.*

data class ActivationToken(
    val userId: UUID,
    val token: UUID = UUID.randomUUID(),
    val expiresAt: LocalDateTime
)
