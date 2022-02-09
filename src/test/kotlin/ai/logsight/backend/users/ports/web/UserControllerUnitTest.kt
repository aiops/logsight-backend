package ai.logsight.backend.users.ports.web

import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.security.UserDetailsServiceImpl
import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import ai.logsight.backend.users.ports.out.persistence.UserType
import io.mockk.mockk
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import java.util.*

@WithMockUser(username = "sasho@sasho.com")
@WebMvcTest(UserController::class)
class UserControllerUnitTest {
    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `should return valid get user response when the user exists`() {
        // given
        val userId = UUID.randomUUID()
        Mockito.`when`(userService.findUserByEmail(FindUserByEmailQuery("sasho@sasho.com")))
            .thenReturn(createUserObject(id = userId))

        // when
        val result = mockMvc.get("/api/v1/users/user")

        // then
        result.andExpect {
            status { isOk() }
//            content {
//                  jsonPath
//            }
        }
    }

    @AfterEach
    fun tearDown() {
    }

    private fun createUserObject(id: UUID) = User(
        id = id,
        email = "sasho@sasho.com",
        password = "",
        key = "",
        activationDate = LocalDateTime.now(),
        dateCreated = LocalDateTime.now(),
        hasPaid = true,
        usedData = 10,
        approachingLimit = false,
        availableData = 10000,
        activated = true,
        userType = UserType.ONLINE_USER)
}
