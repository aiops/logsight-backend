package ai.logsight.backend.token.domain

import java.time.LocalDateTime
import java.util.UUID

data class PasswordResetToken(
    val userId: UUID,
    val token: UUID = UUID.randomUUID(),
    val expiresAt: LocalDateTime
)
