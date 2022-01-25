package ai.logsight.backend.user.service

import ai.logsight.backend.email.service.EmailServiceImpl
import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.token.service.TokenServiceImpl
import ai.logsight.backend.user.ports.out.persistence.UserStorageService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserServiceImplTestUnit {
    private val tokenService: TokenServiceImpl = mockk()
    private val emailService: EmailServiceImpl = mockk()
    private val userStoreService: UserStorageService = mockk()

    @BeforeAll
    fun setup() {
        val activationToken = Token(
            userId = UUID.randomUUID(),
            tokenType = TokenType.PASSWORD_RESET_TOKEN,
            token = UUID.randomUUID(),
            expiresAt = LocalDateTime.now() + Duration.ofMinutes(15)
        )
        every { tokenService.checkActivationToken(activationToken) } returns true
        every { tokenService.checkActivationToken(activationToken) } returns true
        every { tokenService.checkActivationToken(activationToken) } returns true
        every { tokenService.checkActivationToken(activationToken) } returns true
    }

    @Test
    fun `should test services mocks`() {
        // given

        // when

        // then
    }
}
