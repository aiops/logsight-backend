package ai.logsight.backend.application.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.application.ports.web.responses.DeleteApplicationResponse
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext

class ApplicationLifecycleControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var appRepository: ApplicationRepository

    @MockBean
    private lateinit var applicationRPCServiceZeroMq: RPCService

    companion object {
        const val endpoint = "/api/v1/users"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested
    @DisplayName("POST $endpoint/{userId}/applications")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class CreateApplication {
        private val createEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications"

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            appRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
            appRepository.save(TestInputConfig.baseAppEntity)
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

            val response = RPCResponse(
                TestInputConfig.baseAppEntity.id.toString(), "message", 200
            )
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenReturn(response)
            // when\
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
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
                        mapper.writeValueAsString(
                            CreateApplicationResponse(
                                request.applicationName, applicationId
                            )
                        )
                    )
                }
            }
                .andReturn().response.contentAsString
        }

        private fun getInvalidNames(): List<Arguments> {
            return mapOf(
                "Empty String" to "",
                "Invalid symbol '!'" to "application!",
                "uppercase letter" to "Application",
                "dash in name" to "application-32"
            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad request for {0}. Value: \"{1}\"")
        @MethodSource("getInvalidNames")
        fun `should return bad request for invalid input`(
            reason: String,
            name: String
        ) {
            // given
            val request = CreateApplicationRequest(name)
            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        fun `should return error if user is not created`() {
            // given
            val createEndpoint = "$endpoint/${UUID.randomUUID()}/applications"
            val request = CreateApplicationRequest("app_name")
            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exc = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is UserNotFoundException)
        }

        @Test
        fun `should return conflict if App already exists in database`() {
            // given
            val request = CreateApplicationRequest(TestInputConfig.baseAppName)
            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exc = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is ApplicationAlreadyCreatedException)
        }

        @Test
        fun `should delete application if backend fails`() {
            // given
            val appName = "valid_app"
            val request = CreateApplicationRequest(appName)

            // when
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenThrow(ApplicationRemoteException())

            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exc = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is ApplicationRemoteException)
            // check if it is deleted
            Assertions.assertNull(appRepository.findByUserAndName(TestInputConfig.baseUserEntity, appName))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/applications/<appId>")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class DeleteApplication {

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            appRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
            appRepository.save(TestInputConfig.baseAppEntity)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
            appRepository.deleteAll()
        }

        @BeforeEach
        fun initApp() {
            appRepository.deleteAll()
            appRepository.save(TestInputConfig.baseAppEntity)
        }

        @Test
        fun `should delete an Application successfully`() {
            // given
            val appId = TestInputConfig.baseApp.id
            val deleteEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications/$appId"
            val response = RPCResponse(
                TestInputConfig.baseAppEntity.id.toString(), "message", 200
            )
            Mockito.`when`(applicationRPCServiceZeroMq.deleteApplication(any()))
                .thenReturn(response)
            // when\
            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            Assertions.assertNull(
                appRepository.findByIdOrNull(appId)
            ) // check if is deleted from db

            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            DeleteApplicationResponse(
                                TestInputConfig.baseApp.name, TestInputConfig.baseApp.id
                            )
                        )
                    )
                }
            }
        }

        @Test
        fun `should return bad request for invalid input`() {
            // given
            val invalidId = "application"
            val deleteEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications/$invalidId"
            // when
            val response = RPCResponse(
                TestInputConfig.baseAppEntity.id.toString(), "message", 200
            )
            Mockito.`when`(applicationRPCServiceZeroMq.deleteApplication(any()))
                .thenReturn(response)
            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exception is MethodArgumentNotValidException)
        }

        @Test
        @WithMockUser(username = "invalidUser@gmail.com")
        fun `should return error if user is not created`() {
            // given
            val appId = TestInputConfig.baseApp.id
            val deleteEndpoint = "$endpoint/${UUID.randomUUID()}/applications/$appId"
            // when
            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exception is UserNotFoundException)
        }

        @Test
        fun `should return not found if App doesn't exist in database`() {
            // given
            val appId = UUID.randomUUID()
            val deleteEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications/$appId"
            // when
            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exc = result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is ApplicationNotFoundException)
        }

        @Test
        fun `should delete application and throw error if backend fails`() {
            // given
            val appId = TestInputConfig.baseApp.id
            val deleteEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications/$appId"

            // when
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenThrow(ApplicationRemoteException())

            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exc = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is ApplicationRemoteException)
            // check if it is deleted
            Assertions.assertNull(appRepository.findByIdOrNull(appId))
        }

        @Test
        fun `should throw error if application is still CREATING`() {
            // given
            val appId = TestInputConfig.baseApp.id
            val deleteEndpoint = "$endpoint/${TestInputConfig.baseUser.id}/applications/$appId"
            val appCreating = TestInputConfig.baseApp.toApplicationEntity()
            appCreating.status = ApplicationStatus.CREATING
            appRepository.save(appCreating)

            // when
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenThrow(ApplicationRemoteException())

            val result = mockMvc.delete(deleteEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.APPLICATION_JSON
            }

            // then
            val exc = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            assertThat(exc is ApplicationStatusException)
        }
    }
}
