package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTOActions
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.LogStreamZeroMq
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.impl.annotations.SpyK
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") class ApplicationLifecycleControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var appRepository: ApplicationRepository

    @SpyK
    @Autowired
    @Qualifier("ZeroMQ")
    private lateinit var analyticsManagerRPC: AnalyticsManagerRPC

    @MockBean
    private lateinit var logStreamZeroMq: LogStreamZeroMq

    companion object {
        const val baseEmail = "testemail@gmail.com"
        const val endpoint = "/api/v1/applications"
        const val appName = "testApp"
        val userEntity = UserEntity(
            email = baseEmail, password = "testpassword", userType = UserType.ONLINE_USER, activated = true
        )
        val user = userEntity.toUser()

        val baseApp = ApplicationEntity(name = appName, user = userEntity, status = ApplicationStatus.CREATING)
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested @DisplayName("POST /api/v1/applications") @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = baseEmail) inner class GetApplications {

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
            val appDto = ApplicationDTO(user.id, appName, user.key, action = ApplicationDTOActions.CREATE)
//            every { analyticsManagerRPC.createApplication(any()) } returns RPCResponse(
//                "UUID",
//                "message",
//                200
//            )
            // when
            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)).with(
                        SecurityMockMvcRequestPostProcessors.csrf()
                    )
            )
            val applicationId = appRepository.findByUserAndName(userEntity, request.applicationName).get().id
            // then
            result.andExpect {
                status().isCreated
            }.andExpect {
                content().json(
                    mapper.writeValueAsString(
                        CreateApplicationResponse(
                            request.applicationName, applicationId
                        )
                    )
                )
            }.andDo(MockMvcResultHandlers.print())
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
            }.andDo(MockMvcResultHandlers.print())
        }

        @Test
        @WithMockUser(username = "invalidUser@gmail.com")
        fun `should return error if user is not created`() {
            // given
            val request = CreateApplicationRequest(appName)
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
            }.andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should return conflict if App already exists in database`() {
            // given
            val request = CreateApplicationRequest(appName)
            // when
            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)).with(
                        SecurityMockMvcRequestPostProcessors.csrf()
                    )
            )
            // then
            result.andExpect {
                status().isConflict
            }.andDo(MockMvcResultHandlers.print())
        }

        @Test
        fun `should delete application if backend fails`() {
            // given
            val appName = "valid_app"
            val request = CreateApplicationRequest(appName)
            val appDto = ApplicationDTO(user.id, appName, user.key, action = ApplicationDTOActions.CREATE)

            // when
//            every { analyticsManagerRPC.createApplication(appDto) } throws ApplicationRemoteException("msg")
            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)).with(
                        SecurityMockMvcRequestPostProcessors.csrf()
                    )
            )

            // then
            result.andExpect {
                status().isInternalServerError
            }.andDo(MockMvcResultHandlers.print())
            // is deleted from database
            val app = appRepository.findByUserAndName(userEntity, appName)
            Assertions.assertTrue(app.isEmpty)
        }
    }
}
