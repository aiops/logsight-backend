package ai.logsight.backend.users.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.email.domain.service.EmailService
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenRepository
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.users.exceptions.*
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.ActivateUserResponse
import ai.logsight.backend.users.ports.web.response.ChangePasswordResponse
import ai.logsight.backend.users.ports.web.response.CreateUserResponse
import ai.logsight.backend.users.ports.web.response.GetUserResponse
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException
import java.time.Duration
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
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @MockBean
    private lateinit var emailService: EmailService

    companion object {
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        const val endpoint = "/api/v1/users"
        const val newUserEmail = "newUser@email.com"
        private val newUserEntity = UserEntity(email = newUserEmail, password = "password123")
        val newUser = newUserEntity.toUser()
    }

    @Nested
    @DisplayName("GET /api/v1/users/user")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetUser {
        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        private val getUserEndpoint = "$endpoint/user"

        @WithMockUser(username = TestInputConfig.baseEmail)
        @Test
        fun `Valid response when the user exists`() {
            // given
            val expectedResponse = GetUserResponse(id = TestInputConfig.baseUser.id, email = TestInputConfig.baseEmail)
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

        @WithMockUser(username = "invalidAuth@gmail.com")
        @Test
        fun `Bad request if user doesn't exist`() {
            // given

            // when
            val result = mockMvc.get(getUserEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exception = result.andExpect {
                status { isBadRequest() }
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
                status { isForbidden() }
            }
        }
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {
        @BeforeEach
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `User created successfully for valid input`() {
            // given
            val createUserRequest = CreateUserRequest(newUser.email, newUser.password, newUser.password)
            // when
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult != null) // user exists
            Assertions.assertThat(!userResult!!.activated) // user is not activated

            val response = CreateUserResponse(userResult.id, userResult.email)

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
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createUserRequest)
                accept = MediaType.APPLICATION_JSON
            }
            val userResult = userRepository.findByEmail(newUser.email)
            Assertions.assertThat(userResult != null) // user exists
            Assertions.assertThat(!userResult!!.activated) // user is not activated

            val response = CreateUserResponse(userResult.id, userResult.email)

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
            val result = mockMvc.post(endpoint) {
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

            val result = mockMvc.post(endpoint) {
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

            val result = mockMvc.post(endpoint) {
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
    @DisplayName("POST $endpoint/activate")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ActivateUser {
        private val activateEndpoint = "$endpoint/activate"

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
            val activateUserRequest = ActivateUserRequest(user.id, tokenEntity.token)
            val response = ActivateUserResponse(user.id, user.email)
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
        fun `Conflict when the user is already activated`() {
            // given
            val user = TestInputConfig.baseUser
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15))
            tokenRepository.save(tokenEntity)
            val activateUserRequest = ActivateUserRequest(user.id, tokenEntity.token)
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
            Assertions.assertThat(exception is UserAlreadyActivatedException)
        }

        private fun getInvalidActivateRequests(): List<Arguments> {
            return mapOf(
                "Invalid User" to "{\"id\":null, \"activationToken\":\"${UUID.randomUUID()}\"",
                "Invalid token" to "{\"id\":\"${TestInputConfig.baseUser.id}\", \"activationToken\":\"\"}",

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
            Assertions.assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        fun `Conflict when the token has expired`() {
            val user = TestInputConfig.baseUser
            val tokenEntity = TokenEntity(user.id, TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(0))
            tokenRepository.save(tokenEntity)
            val activateUserRequest = ActivateUserRequest(user.id, tokenEntity.token)
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
    @DisplayName("POST $endpoint/change_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ChangePassword {
        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            tokenRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        private val changePasswordEndpoint = "$endpoint/change_password"

        @Test
        fun `OK when password changed sucesfully`() {
            // given
            val user = TestInputConfig.baseUser
            val newPassword = "newPassword"
            val request = ChangePasswordRequest(user.password, newPassword, newPassword)
            val response = ChangePasswordResponse(user.id, user.email)
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
                "Passwords not match" to ChangePasswordRequest(
                    TestInputConfig.basePassword, "password123", "notMatchPassword"
                ),
                "Old password too short" to ChangePasswordRequest("short", "password123", "password123"),
                "New password too short" to ChangePasswordRequest(TestInputConfig.basePassword, "short", "short")

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
            val request = ChangePasswordRequest("WrongOldPassword", "password123", "password123")
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
            val request = ChangePasswordRequest(user.password, "password123", "password123")
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
    @DisplayName("POST $endpoint/reset_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class ResetPassword {
        private val passwordResetEndpoint = "$endpoint/reset_password"

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
            val response = ResetPasswordResponse(user.id, user.email)

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
    @DisplayName("POST $endpoint/forgot_password")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ForgotPassword {
        private val forgotPasswordEndpoint = "$endpoint/forgot_password"

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
        fun `Bad request when user doesn't exist`() {
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
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertThat(exception is UserNotFoundException)
        }
    }

    @Nested
    @DisplayName("POST $endpoint/resend_activation")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ResendActivation {
        private val resendActivationEndpoint = "$endpoint/resend_activation"

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
        fun `Bad request when user doesn't exist`() {
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
                status { isBadRequest() }
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
}
