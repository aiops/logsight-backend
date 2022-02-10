package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.LogStreamZeroMq
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationLifecycleControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var appRepository: ApplicationRepository

    @MockBean
    private lateinit var logStreamZeroMq: LogStreamZeroMq

    companion object {
        val endpoint = "/api/v1/applications"
        val userEntity = UserEntity(
            email = "testemail@gmail.com", password = "testpassword", userType = UserType.ONLINE_USER, activated = true
        )
        val user = userEntity.toUser()

        val baseApp = ApplicationEntity(name = "test-name", user = userEntity, status = ApplicationStatus.CREATING)
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested
    @DisplayName("POST /api/v1/applications")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = "testemail@gmail.com")
    inner class GetApplications {

        @BeforeAll
        fun setup() {
            userRepository.save(userEntity)
            appRepository.save(baseApp)
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
            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)).with(
                        SecurityMockMvcRequestPostProcessors.csrf()
                    )
            )
            val applicationId = appRepository.findByUserAndName(userEntity, request.applicationName)?.id
            // then
            result.andExpect {
                status().isCreated
            }.andExpect {
                content().json(
                    mapper.writeValueAsString(
                        CreateApplicationResponse(
                            request.applicationName, applicationId!!
                        )
                    )
                )
            }
        }

        private fun getInvalidNames(): List<Arguments> {
            return listOf("", "application!", "Application", "application-32").map { x -> Arguments.of(x) }
        }

        @ParameterizedTest(name = "Bad request for name: {0}")
        @MethodSource("getInvalidNames")
        fun `should return bad request for invalid input`(name: String) {
            // given
            val request = CreateApplicationRequest(name)
            // when
            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)).with(
                        SecurityMockMvcRequestPostProcessors.csrf()
                    )
            )
            // then
            result.andExpect {
                status().isBadRequest
            }
        }
    }
}
