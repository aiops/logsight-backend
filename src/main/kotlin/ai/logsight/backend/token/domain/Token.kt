package ai.logsight.backend.token.domain

import ai.logsight.backend.token.persistence.TokenType
import java.time.LocalDateTime
import java.util.*

data class Token(
    val userId: UUID,
    val token: UUID = UUID.randomUUID(),
    val expiresAt: LocalDateTime,
    val tokenType: TokenType
)
