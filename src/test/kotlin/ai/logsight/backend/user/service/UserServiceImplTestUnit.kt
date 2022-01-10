package ai.logsight.backend.user.service

import ai.logsight.backend.exceptions.EmailExistsException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.user.persistence.UserEntity
import ai.logsight.backend.user.persistence.UserRepository
import ai.logsight.backend.user.persistence.UserStorageImpl
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.*

internal class UserServiceImplTestUnit {

    private val repository: UserRepository = mockk()
    val userStorageImpl = UserStorageImpl(repository)

    companion object {
        @JvmStatic
        val email = "test@email.com"
        const val password = "password"
        val userEntity = UserEntity(email = email, password = password)
        val createUserCommand = CreateUserCommand(email, password = password)
    }

    @Nested
    @DisplayName("Create User")
    inner class CreateUser {

        @Test
        fun `should create a user`() {
            // given
            every { repository.save(any()) } returns userEntity
            every { repository.findByEmail(email) } returns Optional.empty()
            // when
            val createdUser = userStorageImpl.createUser(createUserCommand)
            // then
            Assertions.assertThat(createUserCommand.email).isEqualTo(createdUser.email)
            verify(exactly = 1) { repository.save(any()) }
        }

        @Test
        fun `should raise EmailExistsException`() {
            // given
            every { repository.save(any()) } returns userEntity
            every { repository.findByEmail(email) } returns Optional.of(userEntity)
            // when

            // then
            assertThrows(EmailExistsException::class.java) {
                userStorageImpl.createUser(createUserCommand)
            }
        }
    }

    @Nested
    @DisplayName("Activate User")
    inner class ActivateUser {

        @Test
        fun `should activate user`() {
            // given
            every { repository.findByEmail(email) } returns Optional.of(userEntity)
            every { repository.save(any()) } returns userEntity
            // when
            val updatedUser =
                userStorageImpl.activateUser(ActivateUserCommand(email, activationToken = UUID.randomUUID()))

            // then
            assert(updatedUser.activated)
        }

        @Test
        fun `should throw UserNotFound`() {
            // given
            every { repository.findByEmail(email) } returns Optional.empty()

            // when

            // then
            assertThrows(UserNotFoundException::class.java) {
                userStorageImpl.activateUser(ActivateUserCommand(email, activationToken = UUID.randomUUID()))
            }
        }
    }

    @Nested
    @DisplayName("Change Password")
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    inner class ChangePassword {
        @Test
        fun `should change password of user`() {
            // given
            val newPassword = "newPassword"
            val modifyEntity =
                UserEntity(id = userEntity.id, password = password, email = userEntity.email)
            every { repository.save(any()) } returns modifyEntity
            every { repository.findByEmail(email) } returns Optional.of(modifyEntity)

            val changePasswordCommand = ChangePasswordCommand(
                userEntity.email,
                newPassword = newPassword,
                confirmNewPassword = newPassword
            )
            // when
            val userChanged = userStorageImpl.changePassword(changePasswordCommand)
            // then
            print(userChanged.password)
            print(userEntity.password)
            assert(userChanged.password != userEntity.password)
            assert(userChanged.password == newPassword)
        }

        @Test
        fun `should report userNotFound`() {
            // given
            val newPassword = "newPassword"
            val modifyEntity =
                UserEntity(id = userEntity.id, password = password, email = userEntity.email)
            every { repository.save(any()) } returns modifyEntity
            every { repository.findByEmail(email) } returns Optional.empty()
            val changePasswordCommand = ChangePasswordCommand(
                userEntity.email,
                newPassword = newPassword,
                confirmNewPassword = newPassword
            )
            // when

            // then
            assertThrows(UserNotFoundException::class.java) { userStorageImpl.changePassword(changePasswordCommand) }
        }

        @Test
        fun `should raise PasswordsNotMatchException`() {
            // given
            every { repository.findByEmail(email) } returns Optional.of(userEntity)
            val newPassword = "newPassword"
            every { repository.save(any()) } returns userEntity
            val changePasswordCommand = ChangePasswordCommand(
                userEntity.email,
                newPassword = newPassword,
                confirmNewPassword = newPassword + "wrong"
            )
            // when

            // then
            verify(exactly = 0) { repository.save(any()) }
            assertThrows(PasswordsNotMatchException::class.java) { userStorageImpl.changePassword(changePasswordCommand) }
        }
    }
}
