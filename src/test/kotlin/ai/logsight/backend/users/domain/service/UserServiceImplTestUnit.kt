package ai.logsight.backend.users.domain.service

import ai.logsight.backend.email.domain.EmailContext
import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.exceptions.EmailExistsException
import ai.logsight.backend.token.extensions.toToken
import ai.logsight.backend.token.persistence.TokenEntity
import ai.logsight.backend.token.persistence.TokenType
import ai.logsight.backend.token.service.TokenService
import ai.logsight.backend.users.domain.service.command.CreateUserCommand
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.external.ExternalServiceManager
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import ai.logsight.backend.users.ports.out.persistence.UserType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration

internal class UserServiceImplTestUnit {
    private val userStorage: UserStorageService = mockk()
    private val emailService: EmailService = mockk()
    private val tokenService: TokenService = mockk()
    private val externalService: ExternalServiceManager = mockk()
    val userService = UserServiceImpl(userStorage, tokenService, emailService, externalService)

    @Nested
    @DisplayName("User Creation")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        private val createUserCommand = CreateUserCommand(
            email = "testemail@mail.com",
            password = "testpassword"
        )
        private val user = UserEntity(
            email = createUserCommand.email,
            password = createUserCommand.password,
            userType = UserType.ONLINE_USER
        ).toUser()

        @Test
        fun `should create user successfully`() {
            // given
            val token = TokenEntity(
                user.id,
                TokenType.ACTIVATION_TOKEN, Duration.ofMinutes(15)
            ).toToken()
            every { userStorage.findUserByEmail(createUserCommand.email) } returns user
            every { userStorage.createUser(createUserCommand.email, createUserCommand.password) } returns user
            every { tokenService.createActivationToken(user.id) } returns token
            val emailContext = EmailContext(
                userEmail = user.email, token = token, title = "Activate your account"
            )

            every { emailService.sendActivationEmail(emailContext) } returns Unit
            // when
            val savedUser = userService.createUser(createUserCommand)
            // then
            assert(savedUser.email == createUserCommand.email)
            assert(savedUser.password == createUserCommand.password)
            assert(!savedUser.activated)
        }

        @Test
        fun `should raise EmailExistsException if user exists`() {
            // given
            every { userStorage.findUserByEmail(createUserCommand.email) } throws EmailExistsException()

            // when

            // then
            Assertions.assertThrows(EmailExistsException::class.java) { userService.createUser(createUserCommand) }
        }
    }
}
