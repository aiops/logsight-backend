package ai.logsight.backend.token.service

import ai.logsight.backend.token.config.TokenConfigurationProperties
import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.token.exceptions.InvalidTokenTypeException
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

internal class TokenServiceImplTest {
    private val repository: TokenRepository = mockk(relaxed = true)
    val tokenConfig = TokenConfigurationProperties()
    val tokenService = TokenServiceImpl(repository, tokenConfig)

    @Nested
    @DisplayName("Create Token")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateToken {

        @Test
        fun `should create Activation token`() {
            // given
            val userId = UUID.randomUUID()
            val tokenEntity = TokenEntity(
                userId = userId,
                tokenDuration = tokenConfig.duration,
                tokenType = TokenType.ACTIVATION_TOKEN
            )
            every { repository.save(any()) } returns tokenEntity
            // when
            val savedToken = tokenService.createActivationToken(userId)

            // then
            assert(savedToken.tokenType == TokenType.ACTIVATION_TOKEN)
            assert(savedToken.tokenType != TokenType.PASSWORD_RESET_TOKEN)
            assert(savedToken.userId == userId)
        }

        @Test
        fun `should create PasswordResetToken`() {
            // given
            val userId = UUID.randomUUID()
            val tokenEntity = TokenEntity(
                userId = userId,
                tokenDuration = tokenConfig.duration,
                tokenType = TokenType.PASSWORD_RESET_TOKEN
            )
            every { repository.save(any()) } returns tokenEntity

            // when
            val savedToken = tokenService.createPasswordResetToken(userId)

            // then
            assert(savedToken.tokenType == TokenType.PASSWORD_RESET_TOKEN)
            assert(savedToken.tokenType != TokenType.ACTIVATION_TOKEN)
            assert(savedToken.userId == userId)
        }
    }

    @Nested
    @DisplayName("Check token")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CheckToken {

        @Test
        fun `should validate activation and password tokens`() {
            // given
            val userId = UUID.randomUUID()
            val passwordToken = Token(
                userId = userId,
                tokenType = TokenType.PASSWORD_RESET_TOKEN,
                token = UUID.randomUUID(),
                expiresAt = LocalDateTime.now() + tokenConfig.duration
            )
            val activationToken = Token(
                userId = userId,
                tokenType = TokenType.ACTIVATION_TOKEN,
                token = UUID.randomUUID(),
                expiresAt = LocalDateTime.now() + tokenConfig.duration
            )
            every { repository.deleteById(any()) } returns Unit
            // when
            tokenService.checkPasswordResetToken(passwordToken)
            tokenService.checkActivationToken(activationToken)

            // then
        }

        @Test
        fun `should show TokenExpiredException and InvalidTokenTypeException`() {
            // given
            val userId = UUID.randomUUID()
            val passwordToken = Token(
                userId = userId,
                tokenType = TokenType.PASSWORD_RESET_TOKEN,
                token = UUID.randomUUID(),
                expiresAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
            )
            val activationToken = Token(
                userId = userId,
                tokenType = TokenType.ACTIVATION_TOKEN,
                token = UUID.randomUUID(),
                expiresAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
            )
            every { repository.deleteById(any()) } returns Unit

            // when

            // then
            assertThrows(InvalidTokenTypeException::class.java) { tokenService.checkActivationToken(passwordToken) }
            assertThrows(InvalidTokenTypeException::class.java) {
                tokenService.checkPasswordResetToken(activationToken)
            }
            assertThrows(TokenExpiredException::class.java) { tokenService.checkActivationToken(activationToken) }
            assertThrows(TokenExpiredException::class.java) {
                tokenService.checkPasswordResetToken(passwordToken)
            }
        }
    }
}
