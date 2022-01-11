package ai.logsight.backend.user.service

import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.token.service.TokenServiceImpl
import ai.logsight.backend.user.persistence.UserRepository
import ai.logsight.backend.user.persistence.UserStorageService
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
internal class UserServiceImplTestUnit @Autowired constructor(
    private val tokenService: TokenServiceImpl,
    val userRepository: UserRepository,
    @Qualifier("test")
    val emailService: EmailService
) {
    @Test
    fun `should test services mocks`() {
        // given
        val activationToken = Token(
            userId = UUID.randomUUID(),
            tokenType = TokenType.PASSWORD_RESET_TOKEN,
            token = UUID.randomUUID(),
            expiresAt = LocalDateTime.now() + Duration.ofMinutes(15)
        )
        every { tokenService.checkActivationToken(activationToken) } returns true

        // when

        // then
    }
}
