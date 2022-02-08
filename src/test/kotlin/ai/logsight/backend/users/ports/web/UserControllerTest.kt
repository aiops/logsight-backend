package ai.logsight.backend.users.ports.web

import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@WebMvcTest
internal class UserControllerTest {
    @Autowired
    private lateinit var userStorageService: UserStorageService

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private var emailService: EmailService = mockk()

    @Autowired
    private lateinit var externalServices: ExternalServiceManager

    @Autowired
    private lateinit var timeSelectionService: TimeSelectionService

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
}
