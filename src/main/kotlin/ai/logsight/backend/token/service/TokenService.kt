package ai.logsight.backend.token.service

import ai.logsight.backend.token.domain.Token
import java.util.UUID

interface TokenService {
    fun findTokenById(tokenId: UUID): Token
    fun createActivationToken(userId: UUID): Token
    fun checkActivationToken(activationToken: Token): Boolean
    fun createPasswordResetToken(userId: UUID): Token
    fun checkPasswordResetToken(passwordToken: Token): Boolean
}
