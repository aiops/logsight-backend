package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.persistence.ApplicationStatus
import ai.logsight.backend.logs.domain.LogFormat
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class LogsServiceImplTest {

    // TODO: Can this be mocked?
    @Autowired
    lateinit var applicationRepository: ApplicationRepository

    // TODO: Can this be mocked?
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var logsServiceImpl: LogsServiceImpl

    companion object {
        private const val numMessages = 1000
        private const val log = "Hello World"
        private const val source = "test"

        val logMessages = List(numMessages) { log }

        val userEntity = UserEntity(
            email = "testemail@mail.com",
            password = "testpassword",
            userType = UserType.ONLINE_USER
        )
        val user = userEntity.toUser()

        val applicationEntity = ApplicationEntity(
            name = "testapp",
            status = ApplicationStatus.READY,
            user = user.toUserEntity()
        )
        private val application = applicationEntity.toApplication()
    }

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeAll
        fun setup() {
            userRepository.save(userEntity)
            applicationRepository.save(applicationEntity)
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipt = logsServiceImpl.processLogs(
                user,
                application,
                LogFormat.UNKNOWN_FORMAT.toString(),
                "default",
                source,
                logMessages
            )

            // then
            Assertions.assertNotNull(logReceipt)
            Assertions.assertEquals(numMessages.toLong(), logReceipt.logsCount)
            Assertions.assertEquals(source, logReceipt.source)
            Assertions.assertEquals(application.id, logReceipt.application.id)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(userEntity)
            applicationRepository.delete(applicationEntity)
        }
    }
}
