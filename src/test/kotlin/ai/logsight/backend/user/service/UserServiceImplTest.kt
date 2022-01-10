package ai.logsight.backend.user.service

import ai.logsight.backend.email.service.EmailService
import ai.logsight.backend.token.service.TokenServiceImpl
import ai.logsight.backend.user.persistence.UserRepository
import ai.logsight.backend.user.service.command.CreateUserCommand
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@DisplayName("User Management Unit tests")
@SpringBootTest
internal class UserServiceImplTest @Autowired constructor(
    val tokenService: TokenServiceImpl,
    val userRepository: UserRepository,
    val emailService: EmailService,

) {

    companion object {
        @JvmStatic
        val email = "email2@mail.com"
        val password = "password"
    }

    @Nested
    @DisplayName("User Creation")
    inner class UserCreate {
        @Test
        @DirtiesContext
        fun `should create a user`() {
            // given
            val createUserCommand = CreateUserCommand(email = email, password = password)
            // when
            val createdUser = user.createUser(userEntity)

            // then
            verify(exactly = 1) { repository.saveAndFlush(userEntity) }
            assertThat(user.email).isEqualTo(createdUser.email)
        }

        @Test
        @DirtiesContext
        fun `should raise error for email exists`() {
            // given
            every { repository.findByEmail(email) } returns userEntity
            every { repository.saveAndFlush(userEntity) } returns userEntity

            // when

            // then
            assertThrows(EmailExistsException::class.java) { userService.createUser(userEntity) }
        }
    }

    @Nested
    @DisplayName("Find Users")
    inner class FindUser {
        @Test
        fun `should find user successfully`() {
            // given
            every { repository.findByEmail(email) } returns userEntity

            // when
            userService.findByEmail(email)

            // then
            verify(exactly = 1) { repository.findByEmail(email) }
        }

        @Test
        fun `should throw exception for findUserByEmail`() {
            // given
            every { repository.findByEmail(email) } returns null

            // when

            // then
            assertThrows(UserNotFoundException::class.java) { userService.findByEmail(email) }
        }
    }

    @Nested
    @DisplayName("Modify users")
    inner class ModifyUsers {
        @Test
        fun `should modify user`() {
            // given
            every { repository.save(userEntity) } returns userEntity
            // when
            userEntity.activated = false
            val activated = userService.activateUser(userEntity)

            // then
            verify(exactly = 1) { repository.save(userEntity) }
            assertThat(activated.activated).isTrue
        }

        @Test
        @DirtiesContext
        fun `should throw user already activated`() {
            // given
            every { repository.save(userEntity) } returns userEntity
            userEntity.activated = true
            // when
            // then
            assertThrows(UserAlreadyActivatedException::class.java) { userService.activateUser(userEntity) }
        }
    }
}
