package ai.logsight.backend.token.extensions

import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.token.persistence.TokenEntity

fun TokenEntity.toToken() = Token(
    userId = this.userId,
    token = this.token,
    expiresAt = this.expiresAt,
    tokenType = this.tokenType
)
