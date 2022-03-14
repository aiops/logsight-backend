package ai.logsight.backend.common.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import java.net.URI

@SpringBootTest
@ContextConfiguration
@DirtiesContext
internal class CommonConfigPropertiesTest {

    @Autowired
    lateinit var commonConfigProperties: CommonConfigProperties

    companion object {
<<<<<<< HEAD
        const val deployment = "stand-alone"
=======
        val deployment = "stand-alone"
>>>>>>> v1.0.0
        val baseURI = URI("http://localhost:4200")
        const val logsightEmail = "support@logsight.ai"
    }

    @Test
    fun `should verify application-config POJO`() {
        Assertions.assertEquals(deployment, commonConfigProperties.deployment)
        Assertions.assertEquals(baseURI, commonConfigProperties.baseURL)
        Assertions.assertEquals(logsightEmail, commonConfigProperties.logsightEmail)
    }
}
