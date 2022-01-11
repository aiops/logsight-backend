package ai.logsight.backend.common.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.net.URI

@SpringBootTest
@ContextConfiguration
internal class CommonConfigurationPropertiesTest {

    // TODO: This is not optimal. The whole application context is generated to just test whether
    //  CommonConfigurationProperties are assigned correctly. Also the default application properties are used instead
    //  of dedicated testing properties

    @Autowired
    lateinit var commonConfigurationProperties: CommonConfigurationProperties

    companion object {
        val baseURI = URI("http://localhost:4200")
        const val logsightEmail = "info@logsight.ai"
    }

    @Test
    fun `should verify application-config POJO`() {
        Assertions.assertEquals(baseURI, commonConfigurationProperties.baseURL)
        Assertions.assertEquals(logsightEmail, commonConfigurationProperties.logsightEmail)
    }
}
