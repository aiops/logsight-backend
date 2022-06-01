package ai.logsight.backend.compare.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.ports.web.ApplicationLifecycleControllerIntegrationTest
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
internal class CompareControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        const val endpoint = "/api/v1/logs/compare"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class CreateCompare {
        private val createEndpoint =
            "${CompareControllerIntegrationTest.endpoint}"

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            appRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
            appRepository.save(TestInputConfig.baseAppEntityReady)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
            appRepository.deleteAll()
        }

        @Test
        fun `should create a new Application successfully`() {
            // given
            val request = CreateApplicationRequest("application")

            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = ApplicationLifecycleControllerIntegrationTest.mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            Assertions.assertNotNull(
                appRepository.findByUserAndName(
                    TestInputConfig.baseUserEntity, request.applicationName
                )
            )
            val applicationId =
                appRepository.findByUserAndName(TestInputConfig.baseUserEntity, request.applicationName)!!.id
            Assertions.assertEquals(
                appRepository.findById(applicationId)
                    .get().status,
                ApplicationStatus.READY
            )
            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        ApplicationLifecycleControllerIntegrationTest.mapper.writeValueAsString(
                            CreateApplicationResponse(
                                request.applicationName, applicationId
                            )
                        )
                    )
                }
            }
                .andReturn().response.contentAsString
        }
    }



}
