package ai.logsight.backend.logs.demo

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.TestInputConfig.baseUser
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
internal class LogDemoServiceIntegrationTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var demoService: LogDemoService

    @Nested
    @DisplayName("hadoop demo data")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class LoadDemoData {
        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun createHadoopDemoForUser() {
            // given
            val user = baseUser
            // when
            demoService.createHadoopDemoForUser(user)
            // then
        }
    }
}
