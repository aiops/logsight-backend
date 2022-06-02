package ai.logsight.backend.logs.demo

import ai.logsight.backend.TestInputConfig.baseUser
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class LogDemoServiceTest {

    @Autowired
    lateinit var demoService: LogDemoService

    @Test
    fun createHadoopDemoForUser() {
        // given
        val user = baseUser
        // when
        demoService.createHadoopDemoForUser(user)
        // then
    }
}
