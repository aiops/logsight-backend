package ai.logsight.backend.token.service

import ai.logsight.backend.token.config.TokenConfigurationProperties
import ai.logsight.backend.token.persistence.TokenRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*

internal class TokenServiceImplTest {
    private val repository: TokenRepository = mockk(relaxed = true)
    val tokenService = TokenServiceImpl(repository, TokenConfigurationProperties())
}
