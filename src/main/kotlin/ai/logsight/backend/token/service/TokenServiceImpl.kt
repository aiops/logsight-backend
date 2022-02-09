package ai.logsight.backend.token.service

import ai.logsight.backend.token.config.TokenConfigurationProperties
import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.token.exceptions.InvalidTokenTypeException
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.exceptions.TokenNotFoundException
import ai.logsight.backend.token.extensions.toToken
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class TokenServiceImpl(
    private val tokenRepository: TokenRepository,
    private val tokenConfig: TokenConfigurationProperties,
) : TokenService {
    override fun findTokenById(tokenId: UUID): Token {
        return tokenRepository.findById(tokenId)
            .orElseThrow { TokenNotFoundException("Token with id $tokenId does not exist.") }.toToken()
    }

    override fun createActivationToken(userId: UUID): Token {
        return tokenRepository.save(
            TokenEntity(userId = userId, tokenDuration = tokenConfig.duration, tokenType = TokenType.ACTIVATION_TOKEN)
        ).toToken()
    }

    override fun checkActivationToken(activationToken: Token): Boolean {
        return checkToken(activationToken, TokenType.ACTIVATION_TOKEN)
    }

    override fun createPasswordResetToken(userId: UUID): Token {
        return tokenRepository.save(
            TokenEntity(
                userId = userId,
                tokenDuration = tokenConfig.duration,
                tokenType = TokenType.PASSWORD_RESET_TOKEN
            )
        ).toToken()
    }

    override fun checkPasswordResetToken(passwordToken: Token): Boolean {
        return checkToken(passwordToken, TokenType.PASSWORD_RESET_TOKEN)
    }

    private fun checkToken(token: Token, tokenType: TokenType): Boolean {
        // validateAndDelete token
        if (token.tokenType != tokenType) {
            throw InvalidTokenTypeException()
        }
        if (token.expiresAt.isBefore(LocalDateTime.now())) {
            tokenRepository.deleteById(token.token)
            throw TokenExpiredException()
        }

        tokenRepository.deleteById(token.token)
        return true
    }
}
