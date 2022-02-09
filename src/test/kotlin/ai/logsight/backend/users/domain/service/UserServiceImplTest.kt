package ai.logsight.backend.users.domain.service

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.CreateUserCommand
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class UserServiceImplTest(
    @Autowired val userService: UserService
) {

    @Autowired
    private lateinit var userStorageService: UserStorageService

    companion object {
        val email = "testemail@mail.com"
        val password = "testpassword"
        var baseUser: User? = null
    }

    @Nested
    @DisplayName("Create user")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        @BeforeAll
        fun setup() {
            baseUser = userStorageService.createUser(email, password)
        }

        @AfterAll
        fun teardown() {
            //userStorageService.deleteUser(baseUserEntity.id)
        }

        @Nested
        @DisplayName("Create User")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class CreateUserTests {

            @Test
            fun `should Create user sucessfully `() {
                // given
                val createUserCommand = CreateUserCommand(
                    email = "user1@mail.com", password = "testpassword"
                )
//                every { emailService.sendActivationEmail(any()) } returns Unit
                // when

                val user = userService.createUser(createUserCommand)
                // then

                Assertions.assertEquals(user.email, createUserCommand.email)
                Assertions.assertEquals(userStorageService.findUserById(user.id), user)
            }
        }
    }
}
