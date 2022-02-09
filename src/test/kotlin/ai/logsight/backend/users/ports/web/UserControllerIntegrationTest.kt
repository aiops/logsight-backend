package ai.logsight.backend.users.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.AnalyticsManagerZeroMQ
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @MockBean
    private lateinit var analyticsManagerZeroMQ: AnalyticsManagerZeroMQ

    @MockBean
    private lateinit var applicationLifecycleServiceImpl: ApplicationLifecycleServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        tokenRepository.deleteAll()
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid getUser response when the user exists`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        // when
        val result = mockMvc.get("/api/v1/users/user")

        // then
        result.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("{'id':'$userId', 'email':'sasho@sasho.com'}") }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid getUser response when the user does not exist`() {
        // given

        // when
        val result = mockMvc.get("/api/v1/users/user")

        // then
        result.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `should return invalid getUser response when the user is not authenticated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        // when
        val result = mockMvc.get("/api/v1/users/user")

        // then
        result.andExpect {
            status { isForbidden() }
//            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `should return valid createUser response when the user is created`() {
        // given

        // when
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'email':'sasho@sasho.com', 'password':'sasho123', 'repeatPassword':'sasho123'}").with(csrf())
        )
            // then
            .andExpect {
                status().isCreated
            }
    }

    @Test
    fun `should return invalid createUser response when the user request has invalid parameters`() {
        // given
        val parameters = listOf<String>(
            "{'email':'sasho@sasho.com', 'password':'sasho12', 'repeatPassword':'sasho123'}", // not matching passwords
            "{'email':'sashosasho.com', 'password':'sasho123', 'repeatPassword':'sasho123'}", // invalid email
            "{'email':'sashosasho.com', 'password':'sasho13', 'repeatPassword':'sasho123'}", // invalid email and not matching passwords
            "{'email':'sashosasho.com', 'password':'sasho', 'repeatPassword':'sasho'}", // password less than 8 characters
            "{'email':'', 'password':'sasho', 'repeatPassword':'sasho'}", // empty email
            "{'email':'', 'password':'', 'repeatPassword':''}", // empty request parameters
            "{}", // empty request
        )
        // when

        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(parameter).with(csrf())
            )
                // then
                .andExpect {
                    status().isBadRequest
                }
        }
    }

    @Test
    fun `should return valid activateUser response when the user is successfully activated`() {
        // given
        val userId = UUID.randomUUID()
        val token = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, token))
        // when

        mockMvc.perform(
            post("/api/users/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'id: '$userId', 'activationToken':'$token'}").with(csrf())
        )
            // then
            .andExpect {
                status().isOk
            }
    }

    @Test
    fun `should return invalid activateUser response when the user is already activated`() {
        // given
        val userId = UUID.randomUUID()
        val token = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, token))
        // when

        mockMvc.perform(
            post("/api/users/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'id: '$userId', 'activationToken':'$token'}").with(csrf())
        )
            // then
            .andExpect {
                status().isConflict
            }
    }

    @Test
    fun `should return invalid activateUser response when there are invalid request parameters`() {
        // given
        val userId = UUID.randomUUID()
        val token = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, token))

        val parameters = listOf<String>(
            "{'id: '', 'activationToken':'$token'}", // id not given
            "{'id: '$userId', 'activationToken':''}", // activationToken not given
            "{'id: '$userId', 'activationToken':'$userId'}", // wrong activationToken
            "{'id: '$token', 'activationToken':'$token'}", // wrong id
            "{'i: '$token', 'activationToken':'$token'}", // wrong parameter names
            "{'i: '$token', 'activation':'$token'}" // wrong parameter names
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/users/activate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(parameter).with(csrf())
            )
                // then
                .andExpect {
                    status().isBadRequest
                }
        }
    }

    @AfterEach
    fun tearDown() {
    }

    private fun createUserObject(id: UUID, activated: Boolean) = User(
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
        activated = activated,
        userType = UserType.ONLINE_USER
    )

    private fun createTokenObject(userId: UUID, tokenId: UUID) =
        TokenEntity(userId = userId, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
}
