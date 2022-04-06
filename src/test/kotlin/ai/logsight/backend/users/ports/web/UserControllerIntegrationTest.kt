package ai.logsight.backend.users.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.security.authentication.response.GetUserResponse
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.users.exceptions.*
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.external.ExternalElasticsearch
import ai.logsight.backend.users.ports.out.external.exceptions.ExternalServiceException
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.ActivateUserResponse
import ai.logsight.backend.users.ports.web.response.ChangePasswordResponse
import ai.logsight.backend.users.ports.web.response.CreateUserResponse
import ai.logsight.backend.users.ports.web.response.ResetPasswordResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException
import java.time.Duration
import java.util.*
import kotlin.test.assertNull

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class UserControllerIntegrationTest {
    @Autowired
    private lateinit var commonConfigProperties: CommonConfigProperties

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @MockBean
    private lateinit var externalElasticsearch: ExternalElasticsearch

    @MockBean
    private lateinit var emailService: EmailService

    companion object {
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        const val getUserEndpoint = "/api/v1/users"
        const val newUserEmail = "newUser@email.com"
        private val newUserEntity = UserEntity(email = newUserEmail, password = "password123")
        val newUser = newUserEntity.toUser()
    }

    @Nested
    @DisplayName("GET /api/v1/auth/user")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetUser {
        val getUserEndpoint = "/api/v1/auth/user"

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @WithMockUser(username = TestInputConfig.baseEmail)
        @Test
        fun `Valid response when the user exists`() {
            // given
            val expectedResponse = GetUserResponse(userId = TestInputConfig.baseUser.id, TestInputConfig.baseEmail)
            // when
            val result = mockMvc.get(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            expectedResponse
                        )
                    )
                }
            }
        }

        @WithMockUser(username = "notexists@mail.com")
        @Test
        fun `Not found if user doesn't exist`() {
            // given
            // when
            val result = mockMvc.get(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotFoundException)
        }

        @Test
        fun `Forbidden for unauthenticated user`() {
            // given
            val result = mockMvc.get(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isUnauthorized() }
            }
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint (online)")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateOnlineUser {
        @BeforeEach
        fun setUp() {
            commonConfigProperties.deployment = "web-service"
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `User created successfully for valid input`() {
            // given
            val createUserRequest = CreateUserRequest(newUser.email, newUser.password, newUser.password)
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult != null) // user exists
            Assertions.assertThat(!userResult!!.activated) // user is not activated

            val response = CreateUserResponse(userResult.id)

            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
        }

        @Test
        fun `User created even when sendActivationEmail does not work`() {
            // given
            Mockito.`when`(emailService.sendActivationEmail(any()))
                .thenThrow(MailClientException::class.java)
            // given
            val createUserRequest = CreateUserRequest(newUser.email, newUser.password, newUser.password)
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult != null) // user exists
            Assertions.assertThat(!userResult!!.activated) // user is not activated

            val response = CreateUserResponse(userResult.id)

            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
        }

        private fun getInvalidRequests(): List<Arguments> {
            return mapOf(
                "Not Matching passwords" to CreateUserRequest(
                    TestInputConfig.baseEmail, "password", "notMatch"
                ), // not matching passwords,
                "Invalid email" to CreateUserRequest("invalid.com", "password", "password"),
                "invalid email and not match password" to CreateUserRequest(
                    "invalid.com", "password", "notMatch"
                ), // invalid email and not matching passwords
                "password less than 8 characters" to CreateUserRequest(
                    TestInputConfig.baseEmail, "psd", "psd"
                ), // password less than 8 characters
                "empty email" to CreateUserRequest("", "password", "password"), // empty email
                "empty request parameters" to CreateUserRequest("", "", ""), // empty request parameters
            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad request for {0}. ")
        @MethodSource("getInvalidRequests")
        fun `Bad request for invalid input`(
            reason: String,
            request: CreateUserRequest
        ) {

            // given
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        fun `Conflict when user is created but not activated`() {
            // given
            val createdUser = TestInputConfig.baseUser

            val request = CreateUserRequest(
                createdUser.email, createdUser.password, createdUser.password
            )

            // when

            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotActivatedException)
        }

        @Test
        fun `Conflict when the user already exists and is activated`() {
            // given
            val createdUser = TestInputConfig.baseUserEntity
            createdUser.activated = true
            userRepository.save(createdUser)

            val request = CreateUserRequest(
                createdUser.email, createdUser.password, createdUser.password
            )

            // when

            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserExistsException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint (offline)")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        @BeforeEach
        fun setUp() {
            commonConfigProperties.deployment = "stand-alone"
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `User created successfully for valid input`() {
            // given
            val createUserRequest = CreateUserRequest(newUser.email, newUser.password, newUser.password)
//            Mockito.`when`(externalElasticsearch.initialize(anyOrNull()))
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult != null) // user created

            val response = CreateUserResponse(userResult!!.id)

            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
        }

        @Test
        fun `Throws exception if external services do not work`() {
            // given
            val createUserRequest = CreateUserRequest(newUser.email, newUser.password, newUser.password)
            Mockito.`when`(externalElasticsearch.initialize(anyOrNull()))
                .thenThrow(ExternalServiceException::class.java)
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult == null) // user deleted

            val exception = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is ExternalServiceException)
        }

        private fun getInvalidRequests(): List<Arguments> {
            return mapOf(
                "Not Matching passwords" to CreateUserRequest(
                    TestInputConfig.baseEmail, "password", "notMatch"
                ), // not matching passwords,
                "Invalid email" to CreateUserRequest("invalid.com", "password", "password"),
                "invalid email and not match password" to CreateUserRequest(
                    "invalid.com", "password", "notMatch"
                ), // invalid email and not matching passwords
                "password less than 8 characters" to CreateUserRequest(
                    TestInputConfig.baseEmail, "psd", "psd"
                ), // password less than 8 characters
                "empty email" to CreateUserRequest("", "password", "password"), // empty email
                "empty request parameters" to CreateUserRequest("", "", ""), // empty request parameters
            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad request for {0}. ")
        @MethodSource("getInvalidRequests")
        fun `Bad request for invalid input`(
            reason: String,
            request: CreateUserRequest
        ) {

            // given
            // when
            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        fun `Conflict when the user already exists and is activated`() {
            // given
            val createdUser = TestInputConfig.baseUserEntity
            createdUser.activated = true
            userRepository.save(createdUser)

            val request = CreateUserRequest(
                createdUser.email, createdUser.password, createdUser.password
            )

            // when

            val result = mockMvc.post(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserExistsException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint/activate")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ActivateUser {
        private val activateEndpoint = "$getUserEndpoint/activate"

        @BeforeEach
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `Successfully activate user for valid input`() {
            // given
            val user = newUser
            userRepository.save(user.toUserEntity())
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)
            val activateUserRequest = ActivateUserRequest(user.id.toString(), tokenEntity.token)
            val response = ActivateUserResponse(user.id)
            // when
            val result = mockMvc.post(activateEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(activateUserRequest)
                accept = MediaType.APPLICATION_JSON
            }

            val userResult = userRepository.findByEmail(user.email)
            Assertions.assertThat(userResult!!.activated) // user is activated

            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
        }

        @Test
        fun `OK when the user is already activated`() {
            // given
            val user = TestInputConfig.baseUser
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)
            val activateUserRequest = ActivateUserRequest(user.id.toString(), tokenEntity.token)
            // when
            val result = mockMvc.post(activateEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(activateUserRequest)
                accept = MediaType.APPLICATION_JSON
            }

            val userResult = userRepository.findByEmail(user.email)
            Assertions.assertThat(userResult!!.activated) // user is activated

            val exception = result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserAlreadyActivatedException)
        }

        private fun getInvalidActivateRequests(): List<Arguments> {
            return mapOf(
                "Invalid User" to mapper.writeValueAsString(mapOf("activationToken" to UUID.randomUUID())),
                "Invalid token" to mapper.writeValueAsString(
                    mapOf(
                        "id" to TestInputConfig.baseUser.id,
                        "activationToken" to ""
                    )
                ),

            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad activate user request for {0}. ")
        @MethodSource("getInvalidActivateRequests")
        fun `Bad request for invalid input`(reason: String, request: String) {
            // given
            val user = TestInputConfig.baseUser
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)
            // when
            val result = mockMvc.post(activateEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = request
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is HttpMessageNotReadableException)
        }

        @Test
        fun `Conflict when the token has expired`() {
            val user = newUserEntity
            user.activated = false
            userRepository.save(user)
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(0))
            tokenRepository.save(tokenEntity)
            val activateUserRequest = ActivateUserRequest(user.id.toString(), tokenEntity.token)
            // when
            val result = mockMvc.post(activateEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(activateUserRequest)
                accept = MediaType.APPLICATION_JSON
            }

            val userResult = userRepository.findByEmail(user.email)
            Assertions.assertThat(userResult!!.activated) // user is activated

            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is TokenExpiredException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint/change_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ChangePassword {
        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        private val changePasswordEndpoint = "$getUserEndpoint/change_password"

        @Test
        fun `OK when password changed successfully`() {
            // given
            val user = TestInputConfig.baseUser
            val newPassword = "newPassword"
            val request =
                ChangePasswordRequest(user.id.toString(), TestInputConfig.basePassword, newPassword, newPassword)
            val response = ChangePasswordResponse(user.id)
            val result = mockMvc.post(changePasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
            assert(
                passwordEncoder.matches(
                    newPassword, userRepository.findByEmail(user.email)?.password
                )
            )
        }

        private fun getInvalidPasswords(): List<Arguments> {
            return mapOf(
                "Invalid ID" to ChangePasswordRequest(
                    "invalid id", TestInputConfig.basePassword, "password123", "password123"
                ),
                "Passwords not match" to ChangePasswordRequest(
                    TestInputConfig.baseUser.id.toString(),
                    TestInputConfig.basePassword,
                    "password123",
                    "notMatchPassword"
                ),
                "Old password too short" to ChangePasswordRequest(
                    TestInputConfig.baseUser.id.toString(),
                    "short",
                    "password123",
                    "password123"
                ),
                "New password too short" to ChangePasswordRequest(
                    TestInputConfig.baseUser.id.toString(),
                    TestInputConfig.basePassword,
                    "short",
                    "short"
                )

            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad activate user request for {0}. ")
        @MethodSource("getInvalidPasswords")
        fun `Bad request for invalid input`(reason: String, request: ChangePasswordRequest) {
            // given
            val result = mockMvc.post(changePasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        fun `should Error response for wrong old password`() {
            // given
            val request =
                ChangePasswordRequest(
                    TestInputConfig.baseUser.id.toString(),
                    "WrongOldPassword",
                    "password123",
                    "password123"
                )
            val result = mockMvc.post(changePasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is PasswordsNotMatchException)
        }

        @Test
        @WithMockUser(username = newUserEmail)
        fun `Conflict if user is not activated`() {
            // given
            val user = newUserEntity
            user.activated = false
            userRepository.save(user)
            val request =
                ChangePasswordRequest(user.id.toString(), user.password, "password123", "password123")
            val result = mockMvc.post(changePasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotActivatedException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint/reset_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ResetPassword {
        private val passwordResetEndpoint = "$getUserEndpoint/reset_password"

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `OK for successful password reset`() {
            // given
            val user = TestInputConfig.baseUser
            val newPassword = "newPassword"
            val tokenEntity = TokenEntity(user.id, TokenType.PASSWORD_RESET_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)

            val request = ResetPasswordRequest(user.id, newPassword, newPassword, tokenEntity.token)
            val response = ResetPasswordResponse(user.id)

            // when
            val result = mockMvc.post(passwordResetEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(response)
                    )
                }
            }
            assert(
                passwordEncoder.matches(
                    newPassword, userRepository.findByEmail(user.email)?.password
                )
            )
        }

        @Test
        fun `Conflict if token has expired`() {
            val user = TestInputConfig.baseUser
            val tokenEntity = TokenEntity(user.id, TokenType.PASSWORD_RESET_TOKEN, Duration.ofMinutes(0))
            tokenRepository.save(tokenEntity)
            val newPassword = "newPassword"

            val request = ResetPasswordRequest(user.id, newPassword, newPassword, tokenEntity.token)
            // when
            val result = mockMvc.post(passwordResetEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            val userResult = userRepository.findByEmail(user.email)
            Assertions.assertThat(userResult!!.activated) // user is activated

            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is TokenExpiredException)
        }

        @Test
        fun `Conflict if user is not activated`() {
            val user = newUserEntity
            user.activated = false
            userRepository.save(newUserEntity)
            val tokenEntity = TokenEntity(user.id, TokenType.PASSWORD_RESET_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)
            val newPassword = "newPassword"

            val request = ResetPasswordRequest(user.id, newPassword, newPassword, tokenEntity.token)
            // when
            val result = mockMvc.post(passwordResetEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            val userResult = userRepository.findByEmail(user.email)
            Assertions.assertThat(userResult!!.activated) // user is activated

            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotActivatedException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint/forgot_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ForgotPassword {
        private val forgotPasswordEndpoint = "$getUserEndpoint/forgot_password"

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `OK for valid request`() {
            // given
            val user = TestInputConfig.baseUser
            val request = ForgotPasswordRequest(user.email)

            // when
            val result = mockMvc.post(forgotPasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
            }
            val token = tokenRepository.findByUserId(user.id)

            Assertions.assertThat(token.isNotEmpty()) // check if token is created
            Assertions.assertThat(token[0]?.tokenType == TokenType.PASSWORD_RESET_TOKEN)
        }

        @Test
        fun `Not found when user doesn't exist`() {
            // given
            val user = newUser
            val request = ForgotPasswordRequest(user.email)

            // when
            val result = mockMvc.post(forgotPasswordEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotFoundException)
        }
    }

    @Nested
    @DisplayName("POST $getUserEndpoint/resend_activation")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ResendActivation {
        private val resendActivationEndpoint = "$getUserEndpoint/resend_activation"

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `should send activation email for valid request`() {
            // given
            val user = newUserEntity
            user.activated = false
            userRepository.save(user)
            val request = ResendActivationEmailRequest(user.email)

            // when
            val result = mockMvc.post(resendActivationEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
            }
            val token = tokenRepository.findByUserId(user.id)

            Assertions.assertThat(token.isNotEmpty()) // check if token is created
            Assertions.assertThat(token[0]?.tokenType == TokenType.ACTIVATION_TOKEN)
        }

        @Test
        fun `Not found when user doesn't exist`() {
            // given
            val user = newUser
            val request = ResendActivationEmailRequest(user.email)

            // when
            val result = mockMvc.post(resendActivationEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotFoundException)
        }

        @Test
        fun `Conflict if user already activated`() {
            // given
            val user = TestInputConfig.baseUser
            val request = ResendActivationEmailRequest(user.email)

            // when
            val result = mockMvc.post(resendActivationEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserAlreadyActivatedException)
        }
    }

    @Nested
    @DisplayName("DELETE $getUserEndpoint/{userId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class DeletePassword {
        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `should delete user`() {
            // given
            val user = UserEntity(email = "testdelete@delete.com", password = "password123")
            user.activated = true
            externalElasticsearch.initialize(user.toUser())
            userRepository.save(user)

            // when
            val result = mockMvc.delete("$getUserEndpoint/${user.id}") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isNoContent() }
            }
            assertNull(userRepository.findByIdOrNull(user.id))
        }

        @Test
        fun `Not found when user doesn't exist`() {
            // given
            val user = newUser
            val request = ResendActivationEmailRequest(user.email)

            // when
            val result = mockMvc.delete("$getUserEndpoint/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotFoundException)
        }
    }
}
