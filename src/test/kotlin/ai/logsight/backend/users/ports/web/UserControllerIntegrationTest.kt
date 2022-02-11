package ai.logsight.backend.users.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.ports.out.rpc.adapters.zeromq.RPCServiceZeroMq
import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.exceptions.MailClientException
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.CreateUserResponse
import ai.logsight.backend.users.ports.web.response.GetUserResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertTrue

// TODO("Pull out elements in companion objects and inner classes, needs bit of restructuring.")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @MockBean
    private lateinit var emailService: EmailService

    @MockBean
    private lateinit var RPCServiceZeroMq: RPCServiceZeroMq

    @MockBean
    private lateinit var applicationLifecycleServiceImpl: ApplicationLifecycleServiceImpl

    val mapper = ObjectMapper().registerModule(KotlinModule())!!

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
        val expectedResponse = GetUserResponse(id = userId, email = "sasho@sasho.com")
        // when
        val result = mockMvc.get("/api/v1/users/user")

        // then
        result.andExpect {
            status { isOk() }
            content { json(mapper.writeValueAsString(expectedResponse)) }
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
        }.andReturn()
        assertTrue { result.andReturn().response.contentAsString.contains("message") }
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
        }
    }

    @Test
    fun `should return valid createUser response when the user is created`() {
        // given
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        val createUserRequest = CreateUserRequest("sasho@sasho.com", "sasho123", "sasho123")
        // when
        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createUserRequest)).with(csrf())
        )
            // then
            .andExpect {
                assertTrue { it.response.status == HttpStatus.CREATED.value() }
                assertDoesNotThrow { mapper.readValue(it.response.contentAsString, CreateUserResponse::class.java) }
            }
    }

    @Test()
    fun `should return valid createUser response when the user is created but sendActivationEmail does not work`() {
        // given
        Mockito.`when`(emailService.sendActivationEmail(any()))
            .thenThrow(MailClientException::class.java)
        val createUserRequest = CreateUserRequest("sasho@sasho.com", "sasho123", "sasho123")
        // when
        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createUserRequest)).with(csrf())
        )
            // then
            .andExpect {
                assertTrue { it.response.status == HttpStatus.CREATED.value() }
            }
    }

    @Test
    fun `should return invalid createUser response when the user request has invalid parameters`() {
        // given
        val parameters = listOf<CreateUserRequest>(
            CreateUserRequest("sasho@sasho.com", "sasho12", "sasho123"), // not matching passwords
            CreateUserRequest("sashosasho.com", "sasho123", "sasho123"), // invalid email
            CreateUserRequest("sashosasho.com", "sasho13", "sasho123"), // invalid email and not matching passwords
            CreateUserRequest("sashosasho.com", "sasho12", "sasho123"), // password less than 8 characters
            CreateUserRequest("", "sasho123", "sasho123"), // empty email
            CreateUserRequest("", "", ""), // empty request parameters
        )

        // when

        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    @Test
    fun `should return invalid createUser response when the user already exists and is not activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<CreateUserRequest>(
            CreateUserRequest("sasho@sasho.com", "sasho123", "sasho123") // register user that already exists
        )
        // when

        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
                }
        }
    }

    @Test
    fun `should return invalid createUser response when the user already exists and is activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<CreateUserRequest>(
            CreateUserRequest("sasho@sasho.com", "sasho123", "sasho123") // register user that already exists
        )
        // when

        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
                }
        }
    }

    @Test
    fun `should return valid activateUser response when the user is successfully activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token
        val activateUserRequest = ActivateUserRequest(userId, token)
        // when

        mockMvc.perform(
            post("/api/v1/users/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(activateUserRequest)).with(csrf())
        )
            // then
            .andExpect {
                assertTrue { it.response.status == HttpStatus.OK.value() }
            }
    }

    @Test
    fun `should return invalid activateUser response when the user is already activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token
        val activateUserRequest = ActivateUserRequest(userId, token)
        // when

        mockMvc.perform(
            post("/api/v1/users/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(activateUserRequest)).with(csrf())
        )
            // then
            .andExpect {
                assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
            }
    }

    @Test
    fun `should return invalid activateUser response when there are invalid request parameters`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token

        val parameters = listOf<ActivateUserRequest>(
            ActivateUserRequest(userId, userId), // wrong activationToken
            ActivateUserRequest(token, token), // wrong userId
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/activate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    @Test
    fun `should return invalid activateUser response when the token has expired`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        val expired = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token
        val activateUserRequest = ActivateUserRequest(userId, token)
        // when

        mockMvc.perform(
            post("/api/v1/users/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(activateUserRequest)).with(csrf())
        )
            // then
            .andExpect {
                assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
            }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid changePassword response when the password is changed`() {
        // given
        val userId = UUID.randomUUID()
        val token = UUID.randomUUID()
        val activated = true
        val expired = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))

        val parameters = listOf<ChangePasswordRequest>(
            ChangePasswordRequest("sasho123", "sasho1234", "sasho1234")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/change_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.OK.value() }
                    assert(
                        passwordEncoder.matches(
                            "sasho1234",
                            userRepository.findByEmail("sasho@sasho.com")?.password
                        )
                    )
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid changePassword response when the password is changed with wrong parameters`() {
        // given
        val userId = UUID.randomUUID()
        val token = UUID.randomUUID()
        val activated = true
        val expired = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createTokenObject(userId, expired))

        val parameters = listOf<ChangePasswordRequest>(
            ChangePasswordRequest("sasho12", "sasho1234", "sasho1234"),
            ChangePasswordRequest("sasho123", "sasho123", "sasho1234"),
            ChangePasswordRequest("sasho123", "sasho12", "sasho12"),
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/change_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    // tests for resetPassword
    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid resetPassword response when the user resets the password from a password link`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createPasswordTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token

        val parameters = listOf<ResetPasswordRequest>(
            ResetPasswordRequest(userId, "sasho1234", "sasho1234", token)
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/reset_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.OK.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetPassword response when the user resets the password from a password link with invalid parameters`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createPasswordTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token

        val parameters = listOf<ResetPasswordRequest>(
            ResetPasswordRequest(userId, "sasho123", "sasho1234", token),
            ResetPasswordRequest(token, "sasho1234", "sasho1234", token),
            ResetPasswordRequest(userId, "sasho1234", "sasho1234", userId)
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/reset_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetPassword response when the user resets the password from a password link with expired token`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        val expired = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createPasswordTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token

        val parameters = listOf<ResetPasswordRequest>(
            ResetPasswordRequest(userId, "sasho1234", "sasho1234", token)
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/reset_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetPassword response when the user resets the password from a password link but is not activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        val expired = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())
        tokenRepository.save(createPasswordTokenObject(userId, expired))
        val token = tokenRepository.findByUserId(userId).token

        val parameters = listOf<ResetPasswordRequest>(
            ResetPasswordRequest(userId, "sasho1234", "sasho1234", token)
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/reset_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.CONFLICT.value() }
                }
        }
    }

    // tests for forgot_passwords
    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid resetUserPassword response when the user wants to generate a resetPassword email`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<ForgotPasswordRequest>(
            ForgotPasswordRequest("sasho@sasho.com")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/forgot_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.OK.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetUserPassword response when the user wants to generate a resetPassword with non existing email`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<ForgotPasswordRequest>(
            ForgotPasswordRequest("nonexisting@sasho.com")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/forgot_password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    // test for resend_activation
    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return valid resetUserPassword response when the user wants to generate a new activation email`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<ResendActivationEmailRequest>(
            ResendActivationEmailRequest("sasho@sasho.com")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/resend_activation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.OK.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetUserPassword response when the user wants to generate a new activation email with non existing user`() {
        // given
        val userId = UUID.randomUUID()
        val activated = false
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<ResendActivationEmailRequest>(
            ResendActivationEmailRequest("nonexisting@sasho.com")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/resend_activation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    @WithMockUser(username = "sasho@sasho.com")
    @Test
    fun `should return invalid resetUserPassword response when the user wants to generate a new activation email but he is already activated`() {
        // given
        val userId = UUID.randomUUID()
        val activated = true
        userRepository.save(createUserObject(userId, activated).toUserEntity())

        val parameters = listOf<ResendActivationEmailRequest>(
            ResendActivationEmailRequest("nonexisting@sasho.com")
        )
        // when
        for (parameter in parameters) {
            mockMvc.perform(
                post("/api/v1/users/resend_activation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(parameter)).with(csrf())
            )
                // then
                .andExpect {
                    assertTrue { it.response.status == HttpStatus.BAD_REQUEST.value() }
                }
        }
    }

    @AfterEach
    fun tearDown() {
    }

    private fun createUserObject(id: UUID, activated: Boolean) = User(
        id = id,
        email = "sasho@sasho.com",
        password = passwordEncoder.encode("sasho123"),
        key = "qwioemks12kmsdmakms",
        activationDate = LocalDateTime.now(),
        dateCreated = LocalDateTime.now(),
        hasPaid = true,
        usedData = 10,
        approachingLimit = false,
        availableData = 10000,
        activated = activated,
        userType = UserType.ONLINE_USER
    )

    private fun createTokenObject(userId: UUID, expired: Boolean): TokenEntity {
        if (expired) {
            return TokenEntity(userId = userId, TokenType.ACTIVATION_TOKEN, Duration.ofMillis(0))
        } else {
            return TokenEntity(userId = userId, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
        }
    }

    private fun createPasswordTokenObject(userId: UUID, expired: Boolean): TokenEntity {
        if (expired) {
            return TokenEntity(userId = userId, TokenType.PASSWORD_RESET_TOKEN, Duration.ofMillis(0))
        } else {
            return TokenEntity(userId = userId, TokenType.PASSWORD_RESET_TOKEN, Duration.ofMinutes(15))
        }
    }
}
