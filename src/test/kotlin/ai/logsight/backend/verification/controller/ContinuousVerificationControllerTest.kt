package ai.logsight.backend.verification.controller

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.verification.controller.request.LogCompareRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class ContinuousVerificationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var appRepository: ApplicationRepository

    companion object {
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        const val endpoint = "/api/v1/verification"
        val request = LogCompareRequest(applicationId = TestInputConfig.baseApp.id, "base", "compare", "now-180m")
    }

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        appRepository.deleteAll()
        userRepository.save(TestInputConfig.baseUserEntity)
        appRepository.save(TestInputConfig.baseAppEntity)
    }

    @Test
    @WithMockUser(TestInputConfig.baseEmail)
    fun `should return something`() {
        // given

        // when
        mockMvc.get(endpoint) {
            content = mapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }
            .andDo { print() }

        // then
    }
}
