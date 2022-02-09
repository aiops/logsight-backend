package ai.logsight.backend.users.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.AnalyticsManagerZeroMQ
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.ZeroMQPubStream
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

    @MockBean
    private lateinit var zeroMQPubStream: ZeroMQPubStream

    @MockBean
    private lateinit var analyticsManagerZeroMQ: AnalyticsManagerZeroMQ

    @MockBean
    private lateinit var applicationLifecycleServiceImpl: ApplicationLifecycleServiceImpl

    @BeforeEach
    fun setUp() {
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid getUser response when the user exists`() {
        // given
        val userId = UUID.randomUUID()
        userRepository.save(createUserObject(userId).toUserEntity())

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
        userRepository.save(createUserObject(userId).toUserEntity())

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
        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'email':'sasho@sasho.com', 'password':'sasho123', 'repeatPassword':'sasho123'}").with(csrf())
        )
            // then
            .andExpect {
                status().isCreated
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
        userType = UserType.ONLINE_USER
    )
}
