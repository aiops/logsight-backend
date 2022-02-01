package ai.logsight.backend.users.domain.service

import ai.logsight.backend.users.domain.service.command.CreateUserCommand
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
internal class UserServiceImplTest {
    @Autowired
    lateinit var userService: UserService

    @Nested
    @DisplayName("Create user")
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    inner class CreateUser {
        private val createUserCommand = CreateUserCommand(
            email = "testemail@mail.com",
            password = "testpassword"
        )

        @Test
        @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
        fun `should create user successfully`() {
            // given
            // when
            val savedUser = userService.createUser(createUserCommand)

            // then
        }

//        @Test
//        @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//        fun `should throw email exists`() {
//            // given
//
//            // when
//            val savedUser = userService.createUser(createUserCommand)
//
//            // then
//        }
    }
}
